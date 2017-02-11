/** Copyright (c) 2005, Sam Pullara. All Rights Reserved. You may modify and
 * redistribute as long as this attribution remains.
 * <p>
 * Modernized and polished by Yossi Gil yogi@cs.technion.ac.il, 2011. פרופ' יוסי
 * גיל (c)
 * <p>
 * Original copyright remains. Original version can be found <a
 * href=http://code.google.com/p/cli-parser/>here</a>.
 * <p>
*/
package il.org.spartan.external;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Annotation for <code><b>static</b></code> and non-<code><b>static</b></code>
 * data members whose value can be set, externally, i.e., not by usual
 * initialization or via a setter, but from command line arguments.
 * <p>
 * The value of such an annotated field can be extracted from the command line
 * arguments by invoking function
 * {@link Introspector#extract(String[], Object...)} or function
 * {@link Introspector#extract(List, Object...)}. The value can also be
 * extracted from @link{java.util.Properties} by means of
 * {@link Introspector#extract(Properties, Object...)}.
 * <p>
 * @author Sam Pullara.
 * @author Yossi Gil {@literal <yogi@cs.technion.ac.il>}
 * @since 2011-08-20
 * @see Introspector */
@Documented //
@Retention(RetentionPolicy.RUNTIME) //
@Target({ ElementType.FIELD, ElementType.METHOD }) //
public @interface External {
  /** A description of the argument that will appear in the usage method */
  @NotNull String value() default "";

  /** Further description of the argument that will appear in the usage method
   * (if both this and the and the #value field are present, then the full
   * description is obtained by their concatenation, first #value and then this
   * field. */
  @NotNull String description() default "";

  /** Optional name of this command line option, overriding the data member
   * name. */
  @NotNull String name() default "";

  /** An alias for this option */
  @NotNull String alias() default "";

  /** If true, then the option must be set for the parser not to fail */
  boolean required() default false;

  /** A delimiter for arguments that are multi-valued. */
  @NotNull String delimiter() default ",";

  /** Annotation for <code><b>static</b></code> and non-
   * <code><b>static</b></code> array data member whose value is to be set from
   * these command lines which were not consumed to fill data members annotated
   * as {@link External}. Thus, a call to function
   * {@link Introspector#extract(String[], Object...)} (or function
   * {@link Introspector#extract(List, Object...)} for this matter), will
   * initialize any data member marked as {@link Residue} with the residual
   * arguments, i.e., those which were not options.
   * <p>
   * As usual, the array component type must must have a constructor which takes
   * a single {@link String} argument.
   * @author Yossi Gil
   * @since 2011-08-20 */
  @Documented //
  @Retention(RetentionPolicy.RUNTIME) //
  @Target({ ElementType.FIELD, ElementType.METHOD }) //
  @interface Residue {
    //
  }

  class Introspector {
    /** Parse a set of arguments and populate the target with the appropriate
     * values.
     * @param args The arguments you want to parse and populate
     * @param targets An array of items, each being an instance or a class
     *        object, in which {@link External} specifications are to be found.
     *        The first element is interpreted also as the specifier of the main
     *        class.
     * @return The list of arguments that were not consumed
     * @throws Introspector.Argument.ParsingError in case the command line
     *         arguments could not be parsed successfully, i.e., user provided
     *         incorrect input.
     * @throws Introspector.Argument.ReflectionError in case the extracted value
     *         could not be injected into the its targets, which is typically a
     *         result of misuse of this package, e.g., applying an
     *         {@link External} annotation to a <code><b>final</b></code>
     *         field. */
    @NotNull public static List<String> extract(final String[] args, final Object... targets) {
      return extract(cloneAsList(args), targets);
    }

    /** Extract <code>&lt;keyword,value&gt;</code> pairs from a list of
     * arguments, as specified by the {@link External} decorated fields of given
     * object; set these fields, and return the remaining arguments
     * @param arguments command line arguments
     * @param targets An array of items, each being an instance or a class
     *        object, in which {@link External} specifications are to be found.
     *        The first element is interpreted also as the specifier of the main
     *        class.
     * @return the command line arguments, where the
     *         <code>&lt;keyword,value&gt;</code> pairs are removed.
     * @throws Introspector.Argument.ParsingError in case the command line
     *         arguments could not be parsed successfully, i.e., user provided
     *         incorrect input.
     * @throws Introspector.Argument.ReflectionError in case the extracted value
     *         could not be injected into the its targets, which is typically a
     *         result of misuse of this package, e.g., applying an
     *         {@link External} annotation to a <code><b>final</b></code>
     *         field. */
    @NotNull public static List<String> extract(@NotNull final List<String> arguments, @NotNull final Object... targets) {
      @NotNull final List<String> $ = new Introspector().extractInto(arguments, targets);
      residue($, targets);
      return $;
    }

    /** Generate usage information based on annotations.
     * @param targets An array of items, each being an instance or a class
     *        object, in which {@link External} specifications are to be found.
     *        The first element is interpreted also as the specifier of the main
     *        class.
     * @return Usage string */
    @NotNull public static String usage(final Object... targets) {
      return usage(targets[0], "", targets);
    }

    /** Generate usage information based on the target annotations.
     * @param main an instance or a class object, specifying the main class,
     *        from which the application is invoked.
     * @param usage additional usage information
     * @param targets An array of items, each being an instance or a class
     *        object, in which {@link External} specifications are to be found.
     * @return Usage string */
    @NotNull public static String usage(final Object main, final String usage, @NotNull final Object... targets) {
      @NotNull final StringBuilder $ = new StringBuilder("Usage: java " + fullName(main) + " " + usage + "\n");
      for (final Object target : targets)
        usage($, target, getClass(target));
      return $ + "";
    }

    /** Generate usage information based on the target annotations.
     * @param usage additional usage information, usually pertaining to the
     *        non-options
     * @param targets An array of items, each being an instance or a class
     *        object, in which {@link External} specifications are to be found.
     *        The first element is interpreted also as the specifier of the main
     *        class.
     * @return Usage string */
    @NotNull public static String usage(final String usage, final Object... targets) {
      return usage(targets[0], usage, targets);
    }

    /** Prints to the standard error stream usage information text based on the
     * target annotations, and abort.
     * @param targets An array of items, each being an instance or a class
     *        object, in which {@link External} specifications are to be found.
     *        The first element is interpreted also as the specifier of the main
     *        class. */
    public static void usageErrorExit(final Object... targets) {
      usageErrorExit("", targets);
    }

    /** Prints to the standard error stream a usage information text based on
     * the target annotations, and abort.
     * @param usage additional usage information
     * @param targets An array of items, each being an instance or a class
     *        object, in which {@link External} specifications are to be found.
     *        The first element is interpreted also as the specifier of the main
     *        class. */
    public static void usageErrorExit(final String usage, final Object... targets) {
      System.err.print(usage(usage, targets));
      System.exit(1);
    }

    /** Generate a pretty printed string describing the settings of all
     * {@link External} annotated members specified in the parameter. Each of
     * set objects is formatted as its short class name, followed by tab
     * indented lines, each in a key=value form.
     * @param targets An array of items, each being an instance or a class
     *        object, in which {@link External} specifications are to be found.
     *        The first element is interpreted also as the specifier of the main
     *        class.
     * @return a pretty print string describing the current settings of the
     *         parameter. */
    @NotNull public static String settings(@NotNull final Object... targets) {
      @NotNull final StringBuilder $ = new StringBuilder();
      for (final Object target : targets) {
        $.append(shortName(target)).append(":\n");
        for (@NotNull final Entry<String, String> ¢ : toOrderedMap(target).entrySet())
          $.append("\t").append(¢.getKey()).append("=").append(¢.getValue()).append("\n");
      }
      return $ + "";
    }

    /** Convert the settings in the parameter as a set of
     * <code>&lt;String, String&gt;</code> entries, in the order that they were
     * defined.
     * @param targets An array of items, each being an instance or a class
     *        object, in which {@link External} specifications are to be found.
     *        The first element is interpreted also as the specifier of the main
     *        class.
     * @return a {@link Set} of {@link java.util.Map.Entry} objects, each
     *         containing a key,value pair. */
    @NotNull public static Map<String, String> toOrderedMap(@NotNull final Object... targets) {
      @NotNull final Map<String, String> $ = new LinkedHashMap<>();
      for (final Object target : targets) {
        @NotNull final Class<?> c = getClass(target);
        for (@NotNull final PropertyDescriptor ¢ : descriptors(c))
          addEntry($, target, ¢);
        for (@NotNull final Field ¢ : fields(c))
          addEntry($, target, ¢);
        for (@NotNull final Method ¢ : getters(c))
          addEntry($, target, ¢);
      }
      return $;
    }

    private static void addEntry(@NotNull final Map<String, String> m, final Object target, @NotNull final Field f) {
      @Nullable final Argument a = Argument.make(f);
      if (a != null && !m.containsKey(a.name))
        m.put(a.name, a.asString(a.get(target, f)));
    }

    private static void addEntry(@NotNull final Map<String, String> s, final Object target, @NotNull final Method m) {
      @Nullable final Argument a = Argument.make(m);
      if (a != null && !s.containsKey(a.name))
        s.put(a.name, a.asString(a.get(target, m)));
    }

    private static void addEntry(@NotNull final Map<String, String> m, final Object target, @NotNull final PropertyDescriptor d) {
      @Nullable final Argument a = Argument.make(d);
      if (a != null && !m.containsKey(a.name))
        m.put(a.name, a.asString(a.get(target, d)));
    }

    /** Convert the settings in the parameter to a {@link Properties} object.
     * @param targets An array of items, each being an instance or a class
     *        object, in which {@link External} specifications are to be found.
     *        The first element is interpreted also as the specifier of the main
     *        class.
     * @return a {@link Properties} object with the settings of the
     *         parameter. */
    @NotNull public static Properties toProperties(@NotNull final Object... targets) {
      @NotNull final Properties $ = new Properties();
      for (final Object target : targets) {
        @NotNull final Class<?> c = getClass(target);
        for (@NotNull final Field ¢ : fields(c))
          addProperties($, target, ¢);
        for (@NotNull final PropertyDescriptor ¢ : descriptors(c))
          addProperties($, target, ¢);
      }
      return $;
    }

    private final List<Error> errors = new ArrayList<>();

    private static void addProperties(@NotNull final Properties m, final Object target, @NotNull final Field f) {
      @Nullable final Argument a = Argument.make(f);
      if (a != null)
        m.put(a.name, a.asString(a.get(target, f)));
    }

    private static void addProperties(@NotNull final Properties p, final Object target, @NotNull final PropertyDescriptor d) {
      @Nullable final Argument a = Argument.make(d);
      if (a != null)
        p.put(a.name, a.asString(a.get(target, d)));
    }

    @NotNull private static Class<?> getClass(final Object ¢) {
      return ¢ instanceof Class ? (Class<?>) ¢ : ¢.getClass();
    }

    private static String fullName(final Object ¢) {
      return getClass(¢).getName();
    }

    static String shortName(final Object ¢) {
      return getClass(¢).getSimpleName();
    }

    private static void usage(@NotNull final StringBuilder b, final Object target, final Class<?> o) {
      try {
        for (@NotNull final Field ¢ : fields(o))
          b.append(usage(target, ¢));
        for (@NotNull final PropertyDescriptor ¢ : descriptors(o))
          b.append(usage(target, ¢));
      } catch (@NotNull final Error ____) {
        // No point in treating any errors while collecting usage
        // information
      }
    }

    @NotNull private static String usage(final Object target, @NotNull final PropertyDescriptor d) {
      @Nullable final Argument $ = Argument.make(d);
      return $ == null ? "" : $.usage($.get(target, d)) + "\n";
    }

    @NotNull private static String usage(final Object target, @NotNull final Field f) {
      @Nullable final Argument $ = Argument.make(f);
      return $ == null ? "" : $.usage($.get(target, f)) + "\n";
    }

    @NotNull private static ArrayList<String> cloneAsList(final String[] args) {
      return new ArrayList<>(Arrays.asList(args));
    }

    private static void residue(@NotNull final List<String> arguments, @NotNull final Object[] targets) {
      for (final Object target : targets)
        if (!(target instanceof Class))
          residueIntoInstance(target, arguments);
        else
          residueIntoClass(arguments, (Class<?>) target);
    }

    private static void residueIntoInstance(@NotNull final Object target, @NotNull final List<String> arguments) {
      for (@NotNull final Field ¢ : fields(target.getClass()))
        residue(target, ¢, arguments);
    }

    private static void residueIntoClass(@NotNull final List<String> arguments, final Class<?> base) {
      for (Class<?> c = base; c != null; c = c.getSuperclass())
        for (@NotNull final Field ¢ : c.getDeclaredFields())
          residue(c, ¢, arguments);
    }

    private static void residue(final Object target, @NotNull final Field f, @NotNull final List<String> arguments) {
      if (f.getAnnotation(Residue.class) == null)
        return;
      @NotNull final Argument a = Argument.makeResidue(f);
      if (a != null)
        a.set(f, target, arguments);
    }

    @NotNull private List<String> extractInto(@NotNull final List<String> $, @NotNull final Object... targets) {
      for (final Object target : targets)
        if (!(target instanceof Class))
          extractIntoInstance(target, $);
        else
          extractIntoClass((Class<?>) target, $);
      check($);
      wrapErrors(targets);
      return $;
    }

    private void extractIntoClass(final Class<?> base, @NotNull final List<String> arguments) {
      for (@NotNull final PropertyDescriptor ¢ : descriptors(base))
        extractInto(base, ¢, arguments);
      for (Class<?> c = base; c != null; c = c.getSuperclass())
        for (@NotNull final Field ¢ : c.getDeclaredFields())
          extractInto(c, ¢, arguments);
    }

    private void extractIntoInstance(@NotNull final Object target, @NotNull final List<String> arguments) {
      final Class<?> c = target.getClass();
      for (@NotNull final PropertyDescriptor ¢ : descriptors(c))
        extractInto(target, ¢, arguments);
      for (@NotNull final Field ¢ : fields(c))
        extractInto(target, ¢, arguments);
    }

    private static PropertyDescriptor[] descriptors(final Class<?> $) {
      try {
        return java.beans.Introspector.getBeanInfo($).getPropertyDescriptors();
      } catch (@NotNull final IntrospectionException ____) { // Ignore errors of
                                                             // this
        // sort
        return new PropertyDescriptor[0];
      }
    }

    private void wrapErrors(final Object... targets) {
      for (@NotNull final Error ¢ : errors)
        System.err.println(¢.getMessage());
      if (errors.isEmpty())
        return;
      System.err.println(usage(targets));
      throw errors.get(0);
    }

    private void extractInto(final Object target, @NotNull final Field f, @NotNull final List<String> arguments) {
      try {
        @Nullable final Argument a = Argument.make(f);
        if (a != null)
          a.set(f, target, a.extractValue(arguments));
      } catch (@NotNull final Error ¢) {
        errors.add(¢);
      }
    }

    private void extractInto(final Object target, @NotNull final PropertyDescriptor d, @NotNull final List<String> arguments) {
      try {
        @Nullable final Argument a = Argument.make(d);
        if (a != null)
          a.set(d, target, a.extractValue(arguments));
      } catch (@NotNull final Error ¢) {
        errors.add(¢);
      }
    }

    /** Parse properties instead of String arguments. Any additional arguments
     * need to be passed some other way. This is often used in a second pass
     * when the property filename is passed on the command line. Because of
     * required properties you must be careful to set them all in the property
     * file.
     * @param p The properties that contain the arguments
     * @param targets An array of items, each being an instance or a class
     *        object, in which {@link External} specifications are to be found.
     *        The first element is interpreted also as the specifier of the main
     *        class.
     * @throws Introspector.Argument.ParsingError in case the command line
     *         arguments could not be parsed successfully, i.e., user provided
     *         incorrect input.
     * @throws Introspector.Argument.ReflectionError in case the extracted value
     *         could not be injected into the its targets, which is typically a
     *         result of misuse of this package, e.g., applying an
     *         {@link External} annotation to a <code><b>final</b></code>
     *         field. */
    public static void extract(@NotNull final Properties p, @NotNull final Object... targets) {
      new Introspector().extractInto(p, targets);
    }

    private void extractInto(@NotNull final Properties p, @NotNull final Object[] targets) {
      for (final Object target : targets)
        extractInto(p, target);
      wrapErrors(targets);
    }

    private static void extractInto(@NotNull final Properties p, final Object target) {
      @NotNull final Class<?> c = getClass(target);
      for (@NotNull final Field ¢ : fields(c))
        extract(target, ¢, p);
      for (@NotNull final PropertyDescriptor ¢ : descriptors(c))
        extract(target, ¢, p);
    }

    @NotNull private static List<Field> fields(final Class<?> base) {
      @NotNull final ArrayList<Field> $ = new ArrayList<>();
      for (Class<?> ¢ = base; ¢ != null; ¢ = ¢.getSuperclass())
        Collections.addAll($, ¢.getDeclaredFields());
      return $;
    }

    @NotNull private static List<Method> getters(final Class<?> base) {
      @NotNull final ArrayList<Method> $ = new ArrayList<>();
      for (Class<?> c = base; c != null; c = c.getSuperclass())
        for (@NotNull final Method ¢ : c.getDeclaredMethods())
          if (isGetter(¢))
            $.add(¢);
      return $;
    }

    private static boolean isGetter(@NotNull final Method ¢) {
      return ¢.getParameterTypes().length == 0 && ¢.getReturnType() != Void.TYPE;
    }

    private static void extract(final Object target, @NotNull final Field f, @NotNull final Properties p) {
      @Nullable final Argument a = Argument.make(f);
      if (a != null)
        a.set(f, target, a.extractValue(p));
    }

    private static void extract(final Object target, @NotNull final PropertyDescriptor d, @NotNull final Properties p) {
      @Nullable final Argument a = Argument.make(d);
      if (a != null)
        a.set(d, target, a.extractValue(p));
    }

    private void check(@NotNull final List<String> arguments) {
      errors.addAll(arguments.stream().filter(λ -> λ.startsWith("-")).map(UnrecognizedOption::new).collect(Collectors.toList()));
    }

    private abstract static class Error extends RuntimeException {
      public Error(final String message, final Throwable cause) {
        super(message, cause);
      }

      public Error(final String message) {
        super(message);
      }

      private static final long serialVersionUID = 1L;
    }

    public static final class NonArray extends Error {
      NonArray(final String field) {
        super(field + ": is not an array type");
      }

      private static final long serialVersionUID = 1L;
    }

    private abstract static class ArgumentError extends Error {
      public ArgumentError(final String option, final String error, final Throwable cause) {
        super(option + ": " + error, cause);
      }

      public ArgumentError(final String option, final String error) {
        super(option + ": " + error);
      }

      private static final long serialVersionUID = 1L;
    }

    public static class UnrecognizedOption extends ArgumentError {
      public UnrecognizedOption(final String argument) {
        super(argument, "Unrecognized option");
      }

      private static final long serialVersionUID = 1L;
    }

    static class Argument {
      public final String name;
      public final boolean mandatory;
      public final String alias;
      private final String description;
      private final String delimiter;
      private static final String PREFIX = "-";
      public final Class<?> type;

      @Nullable static Argument make(@NotNull final Field ¢) {
        return make(¢.getAnnotation(External.class), ¢.getName(), ¢.getType());
      }

      @Nullable static Argument make(@NotNull final Method ¢) {
        return make(¢.getAnnotation(External.class), ¢.getName(), ¢.getReturnType());
      }

      @NotNull static Argument makeResidue(@NotNull final Field ¢) {
        if (¢.getType().getComponentType() == null)
          throw new NonArray(¢.getName());
        return new Argument(¢.getName(), ¢.getType());
      }

      @NotNull String asString(final Object ¢) {
        return !type.isArray() ? ¢ + "" : arrayValue((Object[]) ¢);
      }

      @NotNull private String arrayValue(@NotNull final Object[] os) {
        @NotNull final StringBuilder $ = new StringBuilder();
        for (final Object ¢ : os)
          $.append($.length() == 0 ? "" : delimiter).append(¢);
        return $ + "";
      }

      static Argument make(@NotNull final PropertyDescriptor ¢) {
        final Method $ = ¢.getWriteMethod();
        return $ == null ? null : Argument.make($.getAnnotation(External.class), ¢.getName(), ¢.getPropertyType());
      }

      @Nullable static Argument make(@Nullable final External x, final String name, final Class<?> type) {
        return x == null ? null : new Argument(x, name, type);
      }

      private Argument(final String name, final Class<?> type) {
        this(name, type, null, false, null, null);
      }

      private Argument(@NotNull final External a, final String defaultName, final Class<?> type) {
        this(defaultsTo(a.name(), defaultName), //
            type, defaultsTo(a.alias(), null), //
            a.required(), //
            a.value() + a.description(), //
            a.delimiter());
      }

      private static String defaultsTo(final String value, final String defaultValue) {
        return !empty(value) ? value : defaultValue;
      }

      private Argument(final String name, final Class<?> type, final String alias, final boolean required, final String description,
          final String delimiter) {
        this.type = type;
        mandatory = required;
        this.alias = alias;
        this.description = description;
        this.delimiter = delimiter;
        this.name = name;
      }

      private Iterator<String> find(@NotNull final List<String> arguments) {
        for (@NotNull final Iterator<String> $ = arguments.iterator(); $.hasNext();)
          if (equals($.next()))
            return $;
        return null;
      }

      @NotNull String extractValue(@NotNull final Properties ¢) {
        return ¢.get(name) != null ? (String) ¢.get(name) : alias != null ? (String) ¢.get(alias) : checkRequired();
      }

      @Nullable String extractValue(@NotNull final List<String> arguments) {
        @Nullable final Iterator<String> $ = find(arguments);
        if ($ == null)
          return checkRequired();
        $.remove();
        if (find(arguments) != null)
          throw new DuplicateOption();
        return extractValue($);
      }

      private boolean equals(@NotNull final String text) {
        if (!text.startsWith(PREFIX))
          return false;
        @NotNull final String $ = text.substring(PREFIX.length());
        return $.equals(name) || alias != null && $.equals(alias);
      }

      private String extractValue(@NotNull final Iterator<String> ¢) {
        if (isBoolean())
          return "true";
        if (!¢.hasNext())
          throw new MissingValueForOption();
        final String $ = ¢.next();
        ¢.remove();
        return $;
      }

      @Nullable String checkRequired() {
        if (mandatory)
          throw new RequiredOption();
        return null;
      }

      void set(@NotNull final Field f, final Object target, @Nullable final String value) {
        if (value != null)
          set(f, target, asObject(value));
      }

      void set(@NotNull final Field f, final Object target, @NotNull final List<String> values) {
        set(f, target, asArrayObject(type.getComponentType(), values));
      }

      void set(@NotNull final Field f, final Object target, @NotNull final Object value) {
        f.setAccessible(true);
        try {
          f.set(target, value);
        } catch (@NotNull final ExceptionInInitializerError ¢) {
          throw new FieldInitializationError(f, value, ¢);
        } catch (@NotNull final IllegalAccessException e) {
          if (Modifier.isFinal(f.getModifiers()))
            throw new FieldIsFinal(f);
        } catch (@NotNull final IllegalArgumentException ¢) {
          throw new WrongTarget(f, value, ¢);
        }
      }

      void set(@NotNull final PropertyDescriptor d, final Object target, @Nullable final String value) {
        if (value != null)
          set(d, target, asObject(value));
      }

      private void set(@NotNull final PropertyDescriptor d, final Object target, final Object value) {
        try {
          d.getWriteMethod().invoke(target, value);
        } catch (@NotNull final InvocationTargetException ¢) {
          throw new FieldConversionError(d, value, ¢);
        } catch (@NotNull final Exception ¢) {
          throw new RuntimeException(¢);
        }
      }

      @Nullable Object get(final Object o, @NotNull final PropertyDescriptor d) {
        final Method $ = d.getReadMethod();
        if ($ == null)
          return null;
        try {
          return $.invoke(o, (Object[]) null);
        } catch (@NotNull final Exception ¢) {
          throw new FieldConversionError(d, ¢);
        }
      }

      Object get(final Object $, @NotNull final Method m) {
        m.setAccessible(true);
        try {
          return m.invoke($, (Object[]) null);
        } catch (@NotNull final Exception ¢) {
          throw new FieldConversionError(m, ¢);
        }
      }

      Object get(final Object $, @NotNull final Field f) {
        f.setAccessible(true);
        try {
          return f.get($);
        } catch (@NotNull final Throwable ¢) {
          throw new FieldUnreadable(f, ¢);
        }
      }

      private static boolean empty(@Nullable final String ¢) {
        return ¢ == null || "".equals(¢);
      }

      @Nullable private Object asObject(@NotNull final String value) {
        return isBoolean() ? Boolean.TRUE
            : type == String.class ? value : !type.isArray() ? instantiate(type, value) : asArrayObject(type.getComponentType(), value);
      }

      private boolean isBoolean() {
        return type == Boolean.class || type == Boolean.TYPE;
      }

      @Nullable private Object asArrayObject(@NotNull final Class<?> c, @NotNull final String value) {
        @NotNull final String[] strings = value.split(delimiter);
        if (c.isPrimitive())
          return asPrimitivesArrayObject(c, strings);
        if (c == String.class)
          return strings;
        @NotNull final Object[] $ = (Object[]) Array.newInstance(c, strings.length);
        for (int ¢ = 0; ¢ < $.length; ++¢)
          $[¢] = instantiate(c, strings[¢]);
        return $;
      }

      private Object asPrimitivesArrayObject(@NotNull final Class<?> c, @NotNull final String[] ss) {
        if (c == byte.class) {
          @NotNull final byte[] $ = (byte[]) Array.newInstance(c, ss.length);
          for (int ¢ = 0; ¢ < $.length; ++¢)
            $[¢] = ((Byte) instantiate(c, ss[¢])).byteValue();
          return $;
        }
        if (c == short.class) {
          @NotNull final short[] $ = (short[]) Array.newInstance(c, ss.length);
          for (int ¢ = 0; ¢ < $.length; ++¢)
            $[¢] = ((Short) instantiate(c, ss[¢])).shortValue();
          return $;
        }
        if (c == int.class) {
          @NotNull final int[] $ = (int[]) Array.newInstance(c, ss.length);
          for (int ¢ = 0; ¢ < $.length; ++¢)
            $[¢] = ((Integer) instantiate(c, ss[¢])).intValue();
          return $;
        }
        if (c == long.class) {
          @NotNull final long[] $ = (long[]) Array.newInstance(c, ss.length);
          for (int ¢ = 0; ¢ < $.length; ++¢)
            $[¢] = ((Long) instantiate(c, ss[¢])).longValue();
          return $;
        }
        if (c == float.class) {
          @NotNull final float[] $ = (float[]) Array.newInstance(c, ss.length);
          for (int ¢ = 0; ¢ < $.length; ++¢)
            $[¢] = ((Float) instantiate(c, ss[¢])).floatValue();
          return $;
        }
        if (c != double.class)
          return null;
        @NotNull final double[] $ = (double[]) Array.newInstance(c, ss.length);
        for (int ¢ = 0; ¢ < $.length; ++¢)
          $[¢] = ((Double) instantiate(c, ss[¢])).doubleValue();
        return $;
      }

      @NotNull private Object asArrayObject(@NotNull final Class<?> c, @NotNull final List<String> values) {
        @NotNull final Object[] $ = (Object[]) Array.newInstance(c, values.size());
        for (int ¢ = 0; ¢ < $.length; ++¢)
          $[¢] = instantiate(c, values.get(¢));
        return $;
      }

      private Object instantiate(@NotNull final Class<?> $, @NotNull final String value) {
        try {
          if ($ == byte.class)
            return Byte.valueOf(value);
          if ($ == short.class)
            return Short.valueOf(value);
          if ($ == int.class)
            return Integer.valueOf(value);
          if ($ == long.class)
            return Long.valueOf(value);
          if ($ == double.class)
            return Double.valueOf(value);
          if ($ == float.class)
            return Float.valueOf(value);
        } catch (@NotNull final NumberFormatException ¢) {
          throw new NumericParsingError(value, ¢);
        }
        return $.isEnum() ? findEnum($, value) : instantiate(getStringConstructor($), value);
      }

      private Object findEnum(@NotNull final Class<?> $, final String value) {
        try {
          return $.getDeclaredMethod("valueOf", String.class).invoke(null, value);
        } catch (@NotNull final InvocationTargetException e) {
          throw new InvalidEnumValue(value);
        } catch (@NotNull final Exception ¢) {
          throw new RuntimeException(¢);
        }
      }

      private Object instantiate(@NotNull final Constructor<?> $, final String value) {
        try {
          return $.newInstance(value);
        } catch (@NotNull final Exception ¢) {
          throw new FieldConversionError($, value, ¢);
        }
      }

      private Constructor<?> getStringConstructor(@NotNull final Class<?> $) {
        try {
          return $.getDeclaredConstructor(String.class);
        } catch (@NotNull final NoSuchMethodException ¢) {
          throw new ConstructorWithSingleStringArgumentMissing($, ¢);
        }
      }

      @NotNull String usage(@Nullable final Object defaultValue) {
        @NotNull final StringBuilder $ = optionName();
        $.append(" [").append(typeName()).append("] ").append(description);
        if (defaultValue == null)
          $.append(defaultValue);
        else {
          $.append(" (");
          if (!type.isArray())
            $.append(defaultValue);
          else {
            @NotNull final List<Object> list = new ArrayList<>();
            final int len = Array.getLength(defaultValue);
            for (int ¢ = 0; ¢ < len; ++¢)
              list.add(Array.get(defaultValue, ¢));
            $.append(list);
          }
          $.append(")");
        }
        if (mandatory)
          $.append(" mandatory");
        return $ + "";
      }

      @NotNull private String typeName() {
        if (isBoolean())
          return "flag";
        if (type.isEnum()) {
          @NotNull final StringBuilder $ = new StringBuilder();
          for (final Object ¢ : type.getEnumConstants()) {
            if ($.length() != 0)
              $.append("|");
            $.append(¢);
          }
          return $ + "";
        }
        if (!type.isArray())
          return shortName(type);
        @NotNull final String componentName = shortName(type.getComponentType());
        @NotNull final StringBuilder $ = new StringBuilder(componentName);
        $.append(" (").append(delimiter).append(componentName).append(")*");
        return $ + "";
      }

      @NotNull private StringBuilder optionName() {
        @NotNull final StringBuilder $ = new StringBuilder("  ").append(PREFIX).append(name);
        if (alias != null)
          $.append(" (").append(PREFIX).append(alias).append(")");
        return $;
      }

      public abstract class Error extends Introspector.ArgumentError {
        public Error(final String error) {
          super(name, error);
        }

        public Error(final String error, final Throwable cause) {
          super(name, error, cause);
        }

        private static final long serialVersionUID = 1L;
      }

      public abstract class ParsingError extends Error {
        ParsingError(final String error) {
          super(error);
        }

        ParsingError(final String error, final Throwable cause) {
          super(error, cause);
        }

        private static final long serialVersionUID = 1L;
      }

      /** An exception thrown in the case of failure to read or set a data
       * member value using Java's reflection.
       * @author Yossi Gil
       * @since 2011-09-01 */
      public abstract class ReflectionError extends Error {
        ReflectionError(final String error) {
          super(error);
        }

        ReflectionError(final String error, final Throwable cause) {
          super(error, cause);
        }

        private static final long serialVersionUID = 1L;
      }

      public class InvalidEnumValue extends ReflectionError {
        InvalidEnumValue(final String value) {
          super("'" + value + "' is not a legal value");
        }

        private static final long serialVersionUID = 1L;
      }

      public class FieldUnreadable extends ReflectionError {
        public FieldUnreadable(final Field f, final Throwable e) {
          super("Cannot read content of field " + f, e);
        }

        private static final long serialVersionUID = 1L;
      }

      public class DuplicateOption extends ParsingError {
        public DuplicateOption() {
          super("duplicate option");
        }

        private static final long serialVersionUID = 1L;
      }

      public class MissingValueForOption extends ParsingError {
        public MissingValueForOption() {
          super("Missing value for this option");
        }

        private static final long serialVersionUID = 1L;
      }

      public class FieldIsFinal extends ReflectionError {
        public FieldIsFinal(final Field f) {
          super("Field " + f + " is final");
        }

        private static final long serialVersionUID = 1L;
      }

      class FieldInitializationError extends ReflectionError {
        public FieldInitializationError(final Field f, @NotNull final Object value, final ExceptionInInitializerError e) {
          super("cannot set field " + f + " to '" + value + "' " + shortName(value.getClass()), e);
        }

        private static final long serialVersionUID = 1L;
      }

      class RequiredOption extends ParsingError {
        public RequiredOption() {
          super("option must be assigned");
        }

        private static final long serialVersionUID = 1L;
      }

      class NumericParsingError extends ParsingError {
        public NumericParsingError(final String value, final NumberFormatException e) {
          super("error parsing value '" + value + "'", e);
        }

        private static final long serialVersionUID = 1L;
      }

      class ConstructorWithSingleStringArgumentMissing extends ReflectionError {
        public ConstructorWithSingleStringArgumentMissing(@NotNull final Class<?> c, final NoSuchMethodException e) {
          super("cannot find " + c.getName() + "(String) constructor", e);
        }

        private static final long serialVersionUID = 1L;
      }

      class FieldConversionError extends ReflectionError {
        public FieldConversionError(@NotNull final Constructor<?> c, final String value, final Exception e) {
          super("'" + value + "' could not be converted into " + shortName(c.getDeclaringClass()), e);
        }

        public FieldConversionError(@NotNull final PropertyDescriptor p, final Object value, final InvocationTargetException e) {
          super("'" + value + "' could not be assigned into " + p.getName(), e);
        }

        public FieldConversionError(@NotNull final PropertyDescriptor p, final Exception e) {
          super("property '" + p.getName() + "' could not be read", e);
        }

        public FieldConversionError(@NotNull final Method m, final Exception e) {
          super("method '" + m.getName() + "' could not be invoked " + e, e);
        }

        private static final long serialVersionUID = 1L;
      }

      class WrongTarget extends ReflectionError {
        public WrongTarget(final Field f, final Object value, final Exception e) {
          super(f + ": could not be assigned value '" + value + "'; try using an instance rather than a class object", e);
        }

        private static final long serialVersionUID = 1L;
      }
    }
  }
}
