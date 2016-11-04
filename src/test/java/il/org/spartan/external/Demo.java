/**
 * Copyright (c) 2005, Sam Pullara. All Rights Reserved. You may modify and ` *
 * redistribute as long as this attribution remains.
 * <p>
 * Modernized and polished by Yossi Gil yogi@cs.technion.ac.il, 2011. Original
 * copyright remains. Original version can be found <a
 * href=http://code.google.com/p/cli-parser/>here</a>.
 */
package il.org.spartan.external;

import static il.org.spartan.external.External.Introspector.*;

import java.io.*;
import java.util.*;

/**
 * A simple demonstration of the capabilities of use of {@link External} and the
 * extraction of command line arguments.
 * <p>
 * Try running it as follows: <code>
 * java il.ac.technion.cs.ssdl.external.Demo all -gender Female -skills
 * painting,coooking -o /tmp/output.txt -firstName Jane -path
 * /bin:/usr/bin:/usr/sbin these arguments are not processed </code> and examine
 * the output
 *
 * @author Sam Pullara.
 * @author Yossi Gil <yogi@cs.technion.ac.il> פרופ' יוסי גיל
 * @since 2011-08-20
 */
public class Demo extends Base {
  // Static field
  @External private static int age = 120;
  // Non-static field
  @External private final int id = 17;
  // Enum field
  @External(required = true) private Gender gender;
  // Array field
  @External("comma separated list of skills") private static String[] skills;
  // Array field with some other delimiter
  @External(value = "colon separated list of files", delimiter = ":") //
  private static File[] path;
  @External.Residue File[] fs;
  @External.Residue static String[] ss;
  @External.Residue static Exception[] es;

  /**
   * main function, to which command line arguments are passed.
   *
   * @param args command line arguments
   */
  public static void main(final String[] args) {
    if (args.length == 0)
      usageErrorExit("name(s)", new Demo());
    demonstrateCommandLineProcessing(args);
    demonstratePropertiesProcessing();
  }
  private static void demonstrateCommandLineProcessing(final String[] args) {
    new Demo().go(args);
  }
  private Demo() {
    // This class is not meant to be instantiated by clients.
  }
  private static void demonstratePropertiesProcessing() {
    final SystemProperties s = new SystemProperties();
    extract(System.getProperties(), s);
    System.out.println(settings(s));
  }
  private void go(final String[] args) {
    printUsageString();
    final List<String> remaining = extract(args, this);
    printExternals();
    printRemaining(remaining);
    printResidue();
    printAutomaticallyGeneratedKeyValueList();
    printPrettyAutomaticallyGeneratedKeyValueList();
  }
  private void printResidue() {
    printResidue(fs);
    printResidue(ss);
    printResidue(es);
  }
  private static void printResidue(final Object[] os) {
    System.out.format("%d remaining arguments injected into a data member of type %s[]:\n", Integer.valueOf(os.length),
        os.getClass().getComponentType().getSimpleName());
    System.out.println("===================================================================");
    for (int ¢ = 0; ¢ < os.length; ++¢)
      System.out.format("\t %d) '%s'\n", Integer.valueOf(¢), os[¢]);
    System.out.println();
  }
  private static void printRemaining(final List<String> remaining) {
    System.out.format("%d remaining arguments:\n", Integer.valueOf(remaining.size()));
    System.out.println("======================");
    for (int ¢ = 0; ¢ < remaining.size(); ++¢)
      System.out.format("\t %d) '%s'\n", Integer.valueOf(¢), remaining.get(¢));
    System.out.println();
  }
  private void printAutomaticallyGeneratedKeyValueList() {
    System.out.println("Automatically generated key,value list:");
    System.out.println("=======================================");
    System.out.println(toProperties(this));
    System.out.println();
  }
  private void printPrettyAutomaticallyGeneratedKeyValueList() {
    System.out.println("Automatically generated key,value string:");
    System.out.println("=========================================");
    System.out.println(settings(this));
    System.out.println();
  }
  private void printUsageString() {
    System.out.println("Usage string:");
    System.out.println("=============");
    System.out.println(usage(this));
    System.out.println();
  }
  private void printExternals() {
    System.out.println("Externals after processing command line arguments:");
    System.out.println("==================================================");
    System.out.println("firstName: " + firstName());
    System.out.println("lastName: " + lastName());
    System.out.println("inputFile: " + inputFile());
    System.out.println("outputFile: " + outputFile());
    System.out.println("age: " + age);
    System.out.println("id: " + id);
    System.out.println("gender: " + gender);
    if (skills != null) {
      System.out.println("Skills:");
      for (final String skill : skills)
        System.out.println("\t* " + skill);
    }
    if (path != null) {
      System.out.println("Path:");
      for (final File ¢ : path)
        System.out.println("\t* " + ¢);
    }
    System.out.println();
  }
}

enum Gender {
  Female, Male
}

class Base {
  // Inherited static fields
  @External private static String firstName;
  @External(alias = "i") private static File inputFile;
  // Inherited non-static fields
  @External private String lastName;
  @External(alias = "o", value = "name of output file") private File outputFile;

  static String firstName() {
    return firstName;
  }
  String lastName() {
    return lastName;
  }
  @SuppressWarnings("static-method") //
  File inputFile() {
    return inputFile;
  }
  File outputFile() {
    return outputFile;
  }
}

class SystemProperties {
  @External(name = "java.vendor", delimiter = ":") static String javaVendor;
  @External(name = "path.separator", delimiter = ":") static String pathSeparator;
  @External(name = "java.class.path", delimiter = ":") static File[] javaPath;
}
