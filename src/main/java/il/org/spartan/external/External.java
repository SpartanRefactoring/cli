/**
 * Copyright (c) 2005, Sam Pullara. All Rights Reserved. You may modify and
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * Annotation for <code><b>static</b></code> and non-<code><b>static</b></code>
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
 *
 * @author Sam Pullara.
 * @author Yossi Gil {@literal <yogi@cs.technion.ac.il>}
 * @since 2011-08-20
 * @see Introspector
 */
@Documented //
@Retention(RetentionPolicy.RUNTIME) //
@Target({ ElementType.FIELD, ElementType.METHOD }) //
public @interface External {
  /**
   * A description of the argument that will appear in the usage method
   */
  String value() default "";
  /**
   * Further description of the argument that will appear in the usage method
   * (if both this and the and the #value field are present, then the full
   * description is obtained by their concatenation, first #value and then this
   * field.
   */
  String description() default "";
  /**
   * Optional name of this command line option, overriding the data member name.
   */
  String name() default "";
  /**
   * An alias for this option
   */
  String alias() default "";
  /**
   * If true, then the option must be set for the parser not to fail
   */
  boolean required() default false;
  /**
   * A delimiter for arguments that are multi-valued.
   */
  String delimiter() default ",";

  /**
   * Annotation for <code><b>static</b></code> and non-
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
   *
   * @author Yossi Gil
   * @since 2011-08-20
   */
  @Documented //
  @Retention(RetentionPolicy.RUNTIME) //
  @Target({ ElementType.FIELD, ElementType.METHOD }) //
  public @interface Residue {
    //
  }

  public static class Introspector {
    /**
     * Parse a set of arguments and populate the target with the appropriate
     * values.
     *
     * @param args The arguments you want to parse and populate
     * @param targets An array of items, each being an instance or a class
     *          object, in which {@link External} specifications are to be
     *          found. The first element is interpreted also as the specifier of
     *          the main class.
     * @return The list of arguments that were not consumed
     * @throws Introspector.Argument.ParsingError in case the command line
     *           arguments could not be parsed successfully, i.e., user provided
     *           incorrect input.
     * @throws Introspector.Argument.ReflectionError in case the extracted value
     *           could not be injected into the its targets, which is typically
     *           a result of misuse of this package, e.g., applying an
     *           {@link External} annotation to a <code><b>final</b></code>
     *           field.
     */
    public static List<String> extract(final String[] args, final Object... targets) {
      return extract(cloneAsList(args), targets);
    }
    /**
     * Extract <code>&lt;keyword,value&gt;</code> pairs from a list of
     * arguments, as specified by the {@link External} decorated fields of given
     * object; set these fields, and return the remaining arguments
     *
     * @param arguments command line arguments
     * @param targets An array of items, each being an instance or a class
     *          object, in which {@link External} specifications are to be
     *          found. The first element is interpreted also as the specifier of
     *          the main class.
     * @return the command line arguments, where the
     *         <code>&lt;keyword,value&gt;</code> pairs are removed.
     * @throws Introspector.Argument.ParsingError in case the command line
     *           arguments could not be parsed successfully, i.e., user provided
     *           incorrect input.
     * @throws Introspector.Argument.ReflectionError in case the extracted value
     *           could not be injected into the its targets, which is typically
     *           a result of misuse of this package, e.g., applying an
     *           {@link External} annotation to a <code><b>final</b></code>
     *           field.
     */
    public static List<String> extract(final List<String> arguments, final Object... targets) {
      final List<String> $ = new Introspector().extractInto(arguments, targets);
      residue($, targets);
      return $;
    }
    /**
     * Generate usage information based on annotations.
     *
     * @param targets An array of items, each being an instance or a class
     *          object, in which {@link External} specifications are to be
     *          found. The first element is interpreted also as the specifier of
     *          the main class.
     * @return Usage string
     */
    public static String usage(final Object... targets) {
      return usage(targets[0], "", targets);
    }
    /**
     * Generate usage information based on the target annotations.
     *
     * @param main an instance or a class object, specifying the main class,
     *          from which the application is invoked.
     * @param usage additional usage information
     * @param targets An array of items, each being an instance or a class
     *          object, in which {@link External} specifications are to be
     *          found.
     * @return Usage string
     */
    public static String usage(final Object main, final String usage, final Object... targets) {
      final StringBuilder $ = new StringBuilder("Usage: java " + fullName(main) + " " + usage + "\n");
      for (final Object target : targets)
        usage($, target, getClass(target));
      return $.toString();
    }
    /**
     * Generate usage information based on the target annotations.
     *
     * @param usage additional usage information, usually pertaining to the
     *          non-options
     * @param targets An array of items, each being an instance or a class
     *          object, in which {@link External} specifications are to be
     *          found. The first element is interpreted also as the specifier of
     *          the main class.
     * @return Usage string
     */
    public static String usage(final String usage, final Object... targets) {
      return usage(targets[0], usage, targets);
    }
    /**
     * Prints to the standard error stream usage information text based on the
     * target annotations, and abort.
     *
     * @param targets An array of items, each being an instance or a class
     *          object, in which {@link External} specifications are to be
     *          found. The first element is interpreted also as the specifier of
     *          the main class.
     */
    public static void usageErrorExit(final Object... targets) {
      usageErrorExit("", targets);
    }
    /**
     * Prints to the standard error stream a usage information text based on the
     * target annotations, and abort.
     *
     * @param usage additional usage information
     * @param targets An array of items, each being an instance or a class
     *          object, in which {@link External} specifications are to be
     *          found. The first element is interpreted also as the specifier of
     *          the main class.
     */
    public static void usageErrorExit(final String usage, final Object... targets) {
      System.err.print(usage(usage, targets));
      System.exit(1);
    }
    /**
     * Generate a pretty printed string describing the settings of all
     * {@link External} annotated members specified in the parameter. Each of
     * set objects is formatted as its short class name, followed by tab
     * indented lines, each in a key=value form.
     *
     * @param targets An array of items, each being an instance or a class
     *          object, in which {@link External} specifications are to be
     *          found. The first element is interpreted also as the specifier of
     *          the main class.
     * @return a pretty print string describing the current settings of the
     *         parameter.
     */
    public static String settings(final Object... targets) {
      final StringBuilder $ = new StringBuilder();
      for (final Object target : targets) {
        $.append(shortName(target) + ":\n");
        for (final Entry<String, String> e : toOrderedMap(target).entrySet())
          $.append("\t").append(e.getKey()).append("=").append(e.getValue()).append("\n");
      }
      return $.toString();
    }
    /**
     * Convert the settings in the parameter as a set of
     * <code>&lt;String, String&gt;</code> entries, in the order that they were
     * defined.
     *
     * @param targets An array of items, each being an instance or a class
     *          object, in which {@link External} specifications are to be
     *          found. The first element is interpreted also as the specifier of
     *          the main class.
     * @return a {@link Set} of {@link java.util.Map.Entry} objects, each
     *         containing a key,value pair.
     */
    public static Map<String, String> toOrderedMap(final Object... targets) {
      final Map<String, String> $ = new LinkedHashMap<>();
      for (final Object target : targets) {
        final Class<? extends Object> c = getClass(target);
        for (final PropertyDescriptor p : descriptors(c))
          addEntry($, target, p);
        for (final Field f : fields(c))
          addEntry($, target, f);
        for (final Method m : getters(c))
          addEntry($, target, m);
      }
      return $;
    }
    private static void addEntry(final Map<String, String> es, final Object target, final Field f) {
      final Argument a = Argument.make(f);
      if (a == null || es.containsKey(a.name))
        return;
      es.put(a.name, a.asString(a.get(target, f)));
    }
    private static void addEntry(final Map<String, String> es, final Object target, final Method m) {
      final Argument a = Argument.make(m);
      if (a == null || es.containsKey(a.name))
        return;
      es.put(a.name, a.asString(a.get(target, m)));
    }
    private static void addEntry(final Map<String, String> es, final Object target, final PropertyDescriptor d) {
      final Argument a = Argument.make(d);
      if (a == null || es.containsKey(a.name))
        return;
      es.put(a.name, a.asString(a.get(target, d)));
    }
    /**
     * Convert the settings in the parameter to a {@link Properties} object.
     *
     * @param targets An array of items, each being an instance or a class
     *          object, in which {@link External} specifications are to be
     *          found. The first element is interpreted also as the specifier of
     *          the main class.
     * @return a {@link Properties} object with the settings of the parameter.
     */
    public static Properties toProperties(final Object... targets) {
      final Properties $ = new Properties();
      for (final Object target : targets) {
        final Class<? extends Object> c = getClass(target);
        for (final Field f : fields(c))
          addProperties($, target, f);
        for (final PropertyDescriptor p : descriptors(c))
          addProperties($, target, p);
      }
      return $;
    }

    private final List<Error> errors = new ArrayList<>();

    private static void addProperties(final Properties m, final Object target, final Field f) {
      final Argument a = Argument.make(f);
      if (a == null)
        return;
      m.put(a.name, a.asString(a.get(target, f)));
    }
    private static void addProperties(final Properties ps, final Object target, final PropertyDescriptor d) {
      final Argument a = Argument.make(d);
      if (a == null)
        return;
      ps.put(a.name, a.asString(a.get(target, d)));
    }
    private static Class<? extends Object> getClass(final Object o) {
      return o instanceof Class ? (Class<?>) o : o.getClass();
    }
    private static String fullName(final Object o) {
      return getClass(o).getName();
    }
    static String shortName(final Object o) {
      return getClass(o).getSimpleName();
    }
    private static void usage(final StringBuilder b, final Object target, final Class<? extends Object> c) {
      try {
        for (final Field f : fields(c))
          b.append(usage(target, f));
        for (final PropertyDescriptor pd : descriptors(c))
          b.append(usage(target, pd));
      } catch (final Error __) {
        // No point in treating any errors while collecting usage
        // information
      }
    }
    private static String usage(final Object target, final PropertyDescriptor p) {
      final Argument a = Argument.make(p);
      return a == null ? "" : a.usage(a.get(target, p)) + "\n";
    }
    private static String usage(final Object target, final Field f) {
      final Argument $ = Argument.make(f);
      return $ == null ? "" : $.usage($.get(target, f)) + "\n";
    }
    private static ArrayList<String> cloneAsList(final String[] args) {
      return new ArrayList<>(Arrays.asList(args));
    }
    private static void residue(final List<String> arguments, final Object[] targets) {
      for (final Object target : targets)
        if (target instanceof Class)
          residueIntoClass(arguments, (Class<?>) target);
        else
          resiudueIntoInstance(target, arguments);
    }
    private static void resiudueIntoInstance(final Object target, final List<String> arguments) {
      for (final Field f : fields(target.getClass()))
        residue(target, f, arguments);
    }
    private static void residueIntoClass(final List<String> arguments, final Class<?> base) {
      for (Class<?> c = base; c != null; c = c.getSuperclass())
        for (final Field f : c.getDeclaredFields())
          residue(c, f, arguments);
    }
    private static void residue(final Object target, final Field f, final List<String> arguments) {
      if (f.getAnnotation(Residue.class) == null)
        return;
      final Argument a = Argument.makeResidue(f);
      if (a != null)
        a.set(f, target, arguments);
    }
    private List<String> extractInto(final List<String> arguments, final Object... targets) {
      for (final Object target : targets)
        if (!(target instanceof Class))
          extractIntoInstance(target, arguments);
        else
          extractIntoClass((Class<?>) target, arguments);
      check(arguments);
      wrapErrors(targets);
      return arguments;
    }
    private void extractIntoClass(final Class<?> base, final List<String> arguments) {
      for (final PropertyDescriptor p : descriptors(base))
        extractInto(base, p, arguments);
      for (Class<?> c = base; c != null; c = c.getSuperclass())
        for (final Field f : c.getDeclaredFields())
          extractInto(c, f, arguments);
    }
    private void extractIntoInstance(final Object target, final List<String> arguments) {
      final Class<? extends Object> c = target.getClass();
      for (final PropertyDescriptor p : descriptors(c))
        extractInto(target, p, arguments);
      for (final Field f : fields(c))
        extractInto(target, f, arguments);
    }
    private static PropertyDescriptor[] descriptors(final Class<? extends Object> c) {
      try {
        return java.beans.Introspector.getBeanInfo(c).getPropertyDescriptors();
      } catch (final IntrospectionException __) { // Ignore errors of this
        // sort
        return new PropertyDescriptor[0];
      }
    }
    private void wrapErrors(final Object... targets) {
      for (final Error e : errors)
        System.err.println(e.getMessage());
      if (errors.size() == 0)
        return;
      System.err.println(usage(targets));
      throw errors.get(0);
    }
    private void extractInto(final Object target, final Field f, final List<String> arguments) {
      try {
        final Argument a = Argument.make(f);
        if (a != null)
          a.set(f, target, a.extractValue(arguments));
      } catch (final Error e) {
        errors.add(e);
      }
    }
    private void extractInto(final Object target, final PropertyDescriptor p, final List<String> arguments) {
      try {
        final Argument a = Argument.make(p);
        if (a != null)
          a.set(p, target, a.extractValue(arguments));
      } catch (final Error e) {
        errors.add(e);
      }
    }
    /**
     * Parse properties instead of String arguments. Any additional arguments
     * need to be passed some other way. This is often used in a second pass
     * when the property filename is passed on the command line. Because of
     * required properties you must be careful to set them all in the property
     * file.
     *
     * @param ps The properties that contain the arguments
     * @param targets An array of items, each being an instance or a class
     *          object, in which {@link External} specifications are to be
     *          found. The first element is interpreted also as the specifier of
     *          the main class.
     * @throws Introspector.Argument.ParsingError in case the command line
     *           arguments could not be parsed successfully, i.e., user provided
     *           incorrect input.
     * @throws Introspector.Argument.ReflectionError in case the extracted value
     *           could not be injected into the its targets, which is typically
     *           a result of misuse of this package, e.g., applying an
     *           {@link External} annotation to a <code><b>final</b></code>
     *           field.
     */
    public static void extract(final Properties ps, final Object... targets) {
      new Introspector().extractInto(ps, targets);
    }
    private void extractInto(final Properties ps, final Object[] targets) {
      for (final Object target : targets)
        extractInto(ps, target);
      wrapErrors(targets);
    }
    private static void extractInto(final Properties ps, final Object target) {
      final Class<?> c = getClass(target);
      for (final Field field : fields(c))
        extract(target, field, ps);
      for (final PropertyDescriptor p : descriptors(c))
        extract(target, p, ps);
    }
    private static List<Field> fields(final Class<?> base) {
      final ArrayList<Field> $ = new ArrayList<>();
      for (Class<?> c = base; c != null; c = c.getSuperclass())
        for (final Field f : c.getDeclaredFields())
          $.add(f);
      return $;
    }
    private static List<Method> getters(final Class<?> base) {
      final ArrayList<Method> $ = new ArrayList<>();
      for (Class<?> c = base; c != null; c = c.getSuperclass())
        for (final Method m : c.getDeclaredMethods())
          if (isGetter(m))
            $.add(m);
      return $;
    }
    private static boolean isGetter(final Method m) {
      return m.getParameterTypes().length == 0 && m.getReturnType() != Void.TYPE;
    }
    private static void extract(final Object target, final Field f, final Properties ps) {
      final Argument a = Argument.make(f);
      if (a == null)
        return;
      a.set(f, target, a.extractValue(ps));
    }
    private static void extract(final Object target, final PropertyDescriptor p, final Properties ps) {
      final Argument a = Argument.make(p);
      if (a == null)
        return;
      a.set(p, target, a.extractValue(ps));
    }
    private void check(final List<String> arguments) {
      for (final String argument : arguments)
        if (argument.startsWith("-"))
          errors.add(new UnrecognizedOption(argument));
    }

    private static abstract class Error extends RuntimeException {
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

    private static abstract class ArgumentError extends Error {
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
      public final boolean required;
      public final String alias;
      private final String description;
      private final String delimiter;
      private static final String PREFIX = "-";
      public final Class<?> type;

      static Argument make(final Field f) {
        return make(f.getAnnotation(External.class), f.getName(), f.getType());
      }
      static Argument make(final Method m) {
        return make(m.getAnnotation(External.class), m.getName(), m.getReturnType());
      }
      static Argument makeResidue(final Field f) {
        if (f.getType().getComponentType() == null)
          throw new NonArray(f.getName());
        return new Argument(f.getName(), f.getType());
      }
      String asString(final Object o) {
        return !type.isArray() ? "" + o : arrayValue((Object[]) o);
      }
      private String arrayValue(final Object[] os) {
        final StringBuilder $ = new StringBuilder();
        for (final Object o : os)
          $.append($.length() == 0 ? "" : delimiter).append(o);
        return $.toString();
      }
      static Argument make(final PropertyDescriptor d) {
        final Method m = d.getWriteMethod();
        return m == null ? null : Argument.make(m.getAnnotation(External.class), d.getName(), d.getPropertyType());
      }
      static Argument make(final External e, final String name, final Class<?> type) {
        return e == null ? null : new Argument(e, name, type);
      }
      private Argument(final String name, final Class<?> type) {
        this(name, type, null, false, null, null);
      }
      private Argument(final External a, final String defaultName, final Class<?> type) {
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
        this.required = required;
        this.alias = alias;
        this.description = description;
        this.delimiter = delimiter;
        this.name = name;
      }
      private Iterator<String> find(final List<String> arguments) {
        for (final Iterator<String> $ = arguments.iterator(); $.hasNext();)
          if (equals($.next()))
            return $;
        return null;
      }
      String extractValue(final Properties ps) {
        if (ps.get(name) != null)
          return (String) ps.get(name);
        if (alias != null)
          return (String) ps.get(alias);
        return checkRequired();
      }
      String extractValue(final List<String> arguments) {
        final Iterator<String> i = find(arguments);
        if (i == null)
          return checkRequired();
        i.remove();
        if (find(arguments) != null)
          throw new DuplicateOption();
        return extractValue(i);
      }
      private boolean equals(final String text) {
        if (!text.startsWith(PREFIX))
          return false;
        final String sansPrefix = text.substring(PREFIX.length());
        return sansPrefix.equals(name) || alias != null && sansPrefix.equals(alias);
      }
      private String extractValue(final Iterator<String> i) {
        if (isBoolean())
          return "true";
        if (!i.hasNext())
          throw new MissingValueForOption();
        final String $ = i.next();
        i.remove();
        return $;
      }
      String checkRequired() {
        if (required)
          throw new RequiredOption();
        return null;
      }
      void set(final Field f, final Object target, final String value) {
        if (value == null)
          return;
        set(f, target, asObject(value));
      }
      void set(final Field f, final Object target, final List<String> values) {
        set(f, target, asArrayObject(type.getComponentType(), values));
      }
      void set(final Field f, final Object target, final Object value) {
        f.setAccessible(true);
        try {
          f.set(target, value);
        } catch (final ExceptionInInitializerError e) {
          throw new FieldInitializationError(f, value, e);
        } catch (final IllegalAccessException e) {
          if (Modifier.isFinal(f.getModifiers()))
            throw new FieldIsFinal(f);
        } catch (final IllegalArgumentException e) {
          throw new WrongTarget(f, value, e);
        }
      }
      void set(final PropertyDescriptor p, final Object target, final String value) {
        if (value == null)
          return;
        set(p, target, asObject(value));
      }
      private void set(final PropertyDescriptor d, final Object target, final Object value) {
        try {
          d.getWriteMethod().invoke(target, value);
        } catch (final InvocationTargetException e) {
          throw new FieldConversionError(d, value, e);
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
      }
      Object get(final Object o, final PropertyDescriptor d) {
        final Method m = d.getReadMethod();
        if (m == null)
          return null;
        try {
          return m.invoke(o, (Object[]) null);
        } catch (final Exception e) {
          throw new FieldConversionError(d, e);
        }
      }
      Object get(final Object o, final Method m) {
        m.setAccessible(true);
        try {
          return m.invoke(o, (Object[]) null);
        } catch (final Exception e) {
          throw new FieldConversionError(m, e);
        }
      }
      Object get(final Object o, final Field f) {
        f.setAccessible(true);
        try {
          return f.get(o);
        } catch (final Throwable e) {
          throw new FieldUnreadable(f, e);
        }
      }
      private static boolean empty(final String s) {
        return s == null || s.equals("");
      }
      private Object asObject(final String value) {
        return isBoolean() ? Boolean.TRUE
            : type == String.class ? value
                : !type.isArray() ? instantiate(type, value) : asArrayObject(type.getComponentType(), value);
      }
      private boolean isBoolean() {
        return type == Boolean.class || type == Boolean.TYPE;
      }
      private Object asArrayObject(final Class<?> c, final String value) {
        final String[] strings = value.split(delimiter);
        if (c.isPrimitive())
          return asPrimitivesArrayObject(c, strings);
        if (c == String.class)
          return strings;
        final Object[] $ = (Object[]) Array.newInstance(c, strings.length);
        for (int i = 0; i < $.length; i++)
          $[i] = instantiate(c, strings[i]);
        return $;
      }
      private Object asPrimitivesArrayObject(final Class<?> c, final String[] strings) {
        if (c == byte.class) {
          final byte[] $ = (byte[]) Array.newInstance(c, strings.length);
          for (int i = 0; i < $.length; i++)
            $[i] = ((Byte) instantiate(c, strings[i])).byteValue();
          return $;
        }
        if (c == short.class) {
          final short[] $ = (short[]) Array.newInstance(c, strings.length);
          for (int i = 0; i < $.length; i++)
            $[i] = ((Short) instantiate(c, strings[i])).shortValue();
          return $;
        }
        if (c == int.class) {
          final int[] $ = (int[]) Array.newInstance(c, strings.length);
          for (int i = 0; i < $.length; i++)
            $[i] = ((Integer) instantiate(c, strings[i])).intValue();
          return $;
        }
        if (c == long.class) {
          final long[] $ = (long[]) Array.newInstance(c, strings.length);
          for (int i = 0; i < $.length; i++)
            $[i] = ((Long) instantiate(c, strings[i])).longValue();
          return $;
        }
        if (c == float.class) {
          final float[] $ = (float[]) Array.newInstance(c, strings.length);
          for (int i = 0; i < $.length; i++)
            $[i] = ((Float) instantiate(c, strings[i])).floatValue();
          return $;
        }
        if (c == double.class) {
          final double[] $ = (double[]) Array.newInstance(c, strings.length);
          for (int i = 0; i < $.length; i++)
            $[i] = ((Double) instantiate(c, strings[i])).doubleValue();
          return $;
        }
        return null;
      }
      private Object asArrayObject(final Class<?> c, final List<String> values) {
        final Object[] $ = (Object[]) Array.newInstance(c, values.size());
        for (int i = 0; i < $.length; i++)
          $[i] = instantiate(c, values.get(i));
        return $;
      }
      private Object instantiate(final Class<?> c, final String value) {
        try {
          if (c == byte.class)
            return new Byte(value);
          if (c == short.class)
            return new Short(value);
          if (c == int.class)
            return new Integer(value);
          if (c == long.class)
            return new Long(value);
          if (c == double.class)
            return new Double(value);
          if (c == float.class)
            return new Float(value);
        } catch (final NumberFormatException e) {
          throw new NumericParsingError(value, e);
        }
        return c.isEnum() ? findEnum(c, value) : instantiate(getStringConstructor(c), value);
      }
      private Object findEnum(final Class<?> c, final String value) {
        try {
          return c.getDeclaredMethod("valueOf", String.class).invoke(null, value);
        } catch (final InvocationTargetException e) {
          throw new InvalidEnumValue(value);
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
      }
      private Object instantiate(final Constructor<?> c, final String value) {
        try {
          return c.newInstance(value);
        } catch (final Exception e) {
          throw new FieldConversionError(c, value, e);
        }
      }
      private Constructor<?> getStringConstructor(final Class<?> c) {
        try {
          return c.getDeclaredConstructor(String.class);
        } catch (final NoSuchMethodException e) {
          throw new ConstructorWithSingleStringArgumentMissing(c, e);
        }
      }
      String usage(final Object defaultValue) {
        final StringBuilder $ = optionName();
        $.append(" [").append(typeName()).append("] ").append(description);
        if (defaultValue == null)
          $.append(defaultValue);
        else {
          $.append(" (");
          if (type.isArray()) {
            final List<Object> list = new ArrayList<>();
            final int len = Array.getLength(defaultValue);
            for (int i = 0; i < len; i++)
              list.add(Array.get(defaultValue, i));
            $.append(list);
          } else
            $.append(defaultValue);
          $.append(")");
        }
        if (required)
          $.append(" mandatory");
        return $.toString();
      }
      private String typeName() {
        if (isBoolean())
          return "flag";
        if (type.isEnum()) {
          final StringBuilder $ = new StringBuilder();
          for (final Object e : type.getEnumConstants()) {
            if ($.length() != 0)
              $.append("|");
            $.append(e);
          }
          return $.toString();
        }
        if (!type.isArray())
          return shortName(type);
        final String componentName = shortName(type.getComponentType());
        final StringBuilder $ = new StringBuilder(componentName);
        $.append(" (").append(delimiter).append(componentName).append(")*");
        return $.toString();
      }
      private StringBuilder optionName() {
        final StringBuilder $ = new StringBuilder("  ").append(PREFIX).append(name);
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

      /**
       * An exception thrown in the case of failure to read or set a data member
       * value using Java's reflection.
       *
       * @author Yossi Gil
       * @since 2011-09-01
       */
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
        public InvalidEnumValue(final String value) {
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
        public FieldInitializationError(final Field f, final Object value, final ExceptionInInitializerError e) {
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
        public ConstructorWithSingleStringArgumentMissing(final Class<?> c, final NoSuchMethodException e) {
          super("cannot find " + c.getName() + "(String) constructor", e);
        }

        private static final long serialVersionUID = 1L;
      }

      class FieldConversionError extends ReflectionError {
        public FieldConversionError(final Constructor<?> c, final String value, final Exception e) {
          super("'" + value + "' could not be converted into " + shortName(c.getDeclaringClass()), e);
        }
        public FieldConversionError(final PropertyDescriptor p, final Object value, final InvocationTargetException e) {
          super("'" + value + "' could not be assigned into " + p.getName(), e);
        }
        public FieldConversionError(final PropertyDescriptor p, final Exception e) {
          super("property '" + p.getName() + "' could not be read", e);
        }
        public FieldConversionError(final Method m, final Exception e) {
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
