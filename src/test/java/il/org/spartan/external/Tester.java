/**
 * Copyright (c) 2005, Sam Pullara. All Rights Reserved. You may modify and
 * redistribute as long as this attribution remains.
 * <p>
 * Modernized and polished by Yossi Gil yogi@cs.technion.ac.il, 2011. Original
 * copyright remains. Original version can be found <a
 * href=http://code.google.com/p/cli-parser/>here</a>.
 */
package il.org.spartan.external;

import static il.org.spartan.external.External.Introspector.*;
import static il.org.spartan.external.RegexMatcher.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

import java.io.*;
import java.security.*;
import java.util.*;

import org.hamcrest.*;
import org.junit.*;

import il.org.spartan.external.External.*;
import il.org.spartan.external.External.Introspector.Argument.*;

/**
 * @author: Sam Pullara.
 * @author: Yossi Gil <yogi@cs.technion.ac.il> פרופ' יוסי גיל
 * @since: Dec 27, 2005 Time: 3:31:44 PM
 */
@SuppressWarnings("static-method") //
public class Tester extends Generator {
  private static final int FIRST____LASTINT____VALUE = 1033096058;

  @Before public void before() {
    oldSecurityManager = System.getSecurityManager();
  }
  @After public void after() {
    System.setSecurityManager(oldSecurityManager);
  }
  @Test public void byteCheck() {
    assert nextByte() != nextByte();
    assertEquals(nextByte(), lastByte());
  }
  @Test public void shortCheck() {
    assert nextShort() != nextShort();
    assertEquals(nextShort(), lastShort());
  }
  @Test public void intCheck() {
    assert nextInt() != nextInt();
    assertEquals(nextInt(), lastInt());
  }
  @Test public void twoLastIntAreIdentical() {
    assertEquals(new Tester().lastInt(), new Tester().lastInt());
  }
  @Test public void myAndOtherLastIntAreIdentical() {
    assertEquals(lastInt(), new Tester().lastInt());
  }
  @Test public void whatIsMyLastInt() {
    assertEquals(FIRST____LASTINT____VALUE, lastInt());
  }
  @Test public void longCheck() {
    assert nextLong() != nextLong();
    assertEquals(nextLong(), lastLong());
  }
  @Test public void floatCheck() {
    assert nextFloat() != nextFloat();
    assertEquals(nextFloat(), lastFloat(), 1E-8);
  }
  @Test public void doubleCheck() {
    assert nextDouble() != nextDouble();
    assertEquals(nextDouble(), lastDouble(), 1E-15);
  }
  @Test public void byteOption() {
    class ____ {
      @External public byte option = nextByte();
    }
    final ____ it = new ____();
    extract(args("-option", nextByte() + ""), it);
    assertEquals(lastByte(), it.option);
  }
  @Test public void shortOption() {
    class ShortOption {
      @External public short s = nextShort();
    }
    final ShortOption s = new ShortOption();
    extract(args("-s", nextShort() + ""), s);
    assertEquals(lastShort(), s.s);
  }
  @Test public void intOption() {
    class ____ {
      @External public int option = nextInt();

      int option() {
        return option;
      }
    }
    final ____ it = new ____();
    extract(args("-option", nextIntS()), it);
    assertEquals(lastInt(), it.option());
  }
  @Test public void longOption() {
    class ____ {
      @External public long option = 123456789;
    }
    final ____ it = new ____();
    extract(args("-option", "9876543210"), it);
    assertEquals(9876543210L, it.option);
  }
  @Test public void floatOption() {
    class ____ {
      @External public float f = nextFloat();
    }
    final ____ ____ = new ____();
    extract(args("-f", nextFloat() + ""), ____);
    assertEquals(lastFloat(), ____.f, 1E-5);
  }
  @Test public void doubleOption() {
    class DoubleOption {
      @External public double d = nextDouble();
    }
    final DoubleOption d = new DoubleOption();
    extract(args("-d", nextDouble() + ""), d);
    assertEquals(lastDouble(), d.d, 1E-8);
  }
  @Test public void integerOption() {
    class option {
      @External public Integer i = Integer.valueOf(nextInt());
    }
    final option i = new option();
    extract(args("-i", nextIntS()), i);
    assertEquals(lastInt(), i.i.intValue());
  }
  @Test public void stringOption() {
    class ____ {
      @External public String s = "This is the time";
    }
    final ____ ____ = new ____();
    extract(args("-s", "for all good men"), ____);
    assertEquals("for all good men", ____.s);
  }
  @Test public void enumOption() {
    extract(args("-option", "EnumB"), ClassWithEnumOption.class);
    assertEquals(EnumType.EnumB, ClassWithEnumOption.option);
  }
  @Test public void integeArrayOption() {
    class option {
      @External public Integer[] is = { Integer.valueOf(nextInt()) };
    }
    final option i = new option();
    extract(args("-is", "1,2,3"), this, i);
    assertEquals(3, i.is.length);
    assertEquals(Integer.valueOf(1), i.is[0]);
    assertEquals(Integer.valueOf(2), i.is[1]);
    assertEquals(Integer.valueOf(3), i.is[2]);
  }
  @Test public void byteArrayOption() {
    class option {
      @External public byte[] bs = { 3, 19, 12, 17 };
    }
    final option o = new option();
    extract(args("-bs", "-33,19,5,1,2,3"), this, o);
    assertEquals(6, o.bs.length);
    int i = 0;
    assertEquals(-33, o.bs[i++]);
    assertEquals(19, o.bs[i++]);
    assertEquals(5, o.bs[i++]);
    assertEquals(1, o.bs[i++]);
    assertEquals(2, o.bs[i++]);
    assertEquals(3, o.bs[i++]);
  }
  @Test public void shortArrayOption() {
    class option {
      @External public short[] ss = { (short) nextInt() };
    }
    final option i = new option();
    extract(args("-ss", "5,1,2,3"), this, i);
    assertEquals(4, i.ss.length);
    assertEquals(5, i.ss[0]);
    assertEquals(1, i.ss[1]);
    assertEquals(2, i.ss[2]);
    assertEquals(3, i.ss[3]);
  }
  @Test public void intArrayOption() {
    class option {
      @External public int[] is = { nextInt() };
    }
    final option i = new option();
    extract(args("-is", "1,2,3"), this, i);
    assertEquals(3, i.is.length);
    assertEquals(1, i.is[0]);
    assertEquals(2, i.is[1]);
    assertEquals(3, i.is[2]);
  }
  @Test public void longArrayOption() {
    class option {
      @External public long[] ls = { (short) nextInt() };
    }
    final option o = new option();
    extract(args("-ls", "19,5,1,2,3"), this, o);
    assertEquals(5, o.ls.length);
    int i = 0;
    assertEquals(19, o.ls[i++]);
    assertEquals(5, o.ls[i++]);
    assertEquals(1, o.ls[i++]);
    assertEquals(2, o.ls[i++]);
    assertEquals(3, o.ls[i++]);
  }
  @Test public void floatArrayOption() {
    class option {
      @External public float[] fs = { 3, 19, 12, 17 };
    }
    final option o = new option();
    extract(args("-fs", "11.2,-33,19,5,1,2,3"), this, o);
    assertEquals(7, o.fs.length);
    int i = 0;
    assertEquals(11.2, o.fs[i++], 1E-5);
    assertEquals(-33, o.fs[i++], 1E-5);
    assertEquals(19, o.fs[i++], 1E-5);
    assertEquals(5, o.fs[i++], 1E-5);
    assertEquals(1, o.fs[i++], 1E-5);
    assertEquals(2, o.fs[i++], 1E-5);
    assertEquals(3, o.fs[i++], 1E-5);
  }
  @Test public void doubleArrayOption() {
    class option {
      @External public double[] ds = { 3, 19, 12, 17 };
    }
    final option o = new option();
    extract(args("-ds", "-32.1,11.2,-33,19,5,1,2,3"), this, o);
    assertEquals(8, o.ds.length);
    int i = 0;
    assertEquals(-32.1, o.ds[i++], 1E-5);
    assertEquals(11.2, o.ds[i++], 1E-5);
    assertEquals(-33, o.ds[i++], 1E-5);
    assertEquals(19, o.ds[i++], 1E-5);
    assertEquals(5, o.ds[i++], 1E-5);
    assertEquals(1, o.ds[i++], 1E-5);
    assertEquals(2, o.ds[i++], 1E-5);
    assertEquals(3, o.ds[i++], 1E-5);
  }
  @Test public void setterFunctionInt() {
    class ____ {
      private int option;

      public int getOption() {
        return option;
      }
      @External(name = "input") //
      public void setOption(final int option) {
        this.option = option;
      }
    }
    final ____ ____ = new ____();
    ____.setOption(nextInt());
    extract(args("-input", nextIntS()), ____);
    assertEquals(lastInt(), ____.getOption());
  }
  @Test public void unNamedSetterFunctionInt() {
    class ____ {
      private int option;

      public int getOption() {
        return option;
      }
      @External //
      public void setOption(final int inputFilename) {
        option = inputFilename;
      }
    }
    final ____ ____ = new ____();
    ____.setOption(nextInt());
    extract(args("-option", nextIntS()), ____);
    assertEquals(lastInt(), ____.getOption());
  }

  static class StaticPrivateField {
    @External private static Integer num = Integer.valueOf(173);

    public static Integer getNum() {
      return num;
    }
  }

  @Test public void staticPrivateFieldThroughClass() {
    extract(args("-num", nextIntS()), StaticPrivateField.class);
    assertEquals(lastInt(), StaticPrivateField.getNum().intValue());
  }
  @Test public void staticPrivateThroughInstance() {
    extract(args("-num", nextIntS()), new StaticPrivateField());
    assertEquals(lastInt(), StaticPrivateField.getNum().intValue());
  }
  @Test public void requiredStaticFields() {
    final List<String> leftOver = extract(args(//
        "-output", "outputValue", //
        "-key", "keyValue" //
    ), RequiredStaticFields.class);
    assertEquals(0, leftOver.size());
    assert RequiredStaticFields.key != null;
    assert RequiredStaticFields.output != null;
    assertEquals("outputValue", RequiredStaticFields.output);
    assertEquals("keyValue", RequiredStaticFields.key);
  }
  @Test public void mixedOptions() {
    class ____ {
      private String inputFilename;

      public String getOption() {
        return inputFilename;
      }
      @External(name = "input", value = "This is the input file", required = true) //
      public void setInputFilename(final String inputFilename) {
        this.inputFilename = inputFilename;
      }

      @External(name = "output", alias = "o", value = "This is the output file", required = true) //
      private File outputFile;

      public File getOutputFile() {
        return outputFile;
      }

      private boolean someotheroption;

      public boolean isSomeotheroption() {
        return someotheroption;
      }

      private boolean someoption;

      public boolean isSomeoption() {
        return someoption;
      }
      @External("This option can optionally be set") public void setSomeoption(final boolean someoption) {
        this.someoption = someoption;
      }
      @External("This option can optionally be set") public void setSomeotheroption(final boolean someotheroption) {
        this.someotheroption = someotheroption;
      }

      private Integer minimum;

      public Integer getMinimum() {
        return minimum;
      }
      @External(value = "Minimum", alias = "m") //
      public void setMinimum(final Integer minimum) {
        this.minimum = minimum;
      }

      private Integer[] values;

      public Integer[] getValues() {
        return values;
      }
      @External(value = "List of values", delimiter = ":") //
      public void setValues(final Integer[] values) {
        this.values = values;
      }
    }
    final ____ ____ = new ____();
    final List<String> extra = extract(args( //
        "-input", "inputfile", //
        "-o", "outputfile", //
        "extra1", //
        "-someoption", //
        "extra2", //
        "-m", "10", //
        "-values", "1:2:3"//
    ), ____);
    assertEquals("inputfile", ____.getOption());
    assertEquals(new File("outputfile"), ____.getOutputFile());
    assert !____.isSomeotheroption();
    assert ____.isSomeoption();
    assertEquals(10, ____.getMinimum().intValue());
    assertEquals(3, ____.getValues().length);
    assertEquals(1, ____.getValues()[0].intValue());
    assertEquals(2, ____.getValues()[1].intValue());
    assertEquals(3, ____.getValues()[2].intValue());
    assertEquals(2, extra.size());
  }
  @Test public void setterFunctionString() {
    class ____ {
      private String inputFilename;

      public String getOption() {
        return inputFilename;
      }
      @External(name = "input") //
      public void setInputFilename(final String inputFilename) {
        this.inputFilename = inputFilename;
      }
    }
    final ____ ____ = new ____();
    extract(args("-input", "inputfile"), ____);
    assertEquals("inputfile", ____.getOption());
  }
  @Test public void remainingArguments() {
    final TestCommand tc = new TestCommand();
    final List<String> extra = extract(args( //
        "this", //
        "-input", "inputfile", //
        "is", "-o", "outputfile", //
        "the", //
        "-someoption", //
        "time", //
        "-m", "10", //
        "for", "all", //
        "-values", "1:2:3", //
        "-strings", "sam;dave;jolly", //
        "good", "men"//
    ), tc);
    assertEquals("inputfile", tc.inputFilename);
    assertEquals(new File("outputfile"), tc.outputFile);
    assert tc.someoption;
    assertEquals(10, tc.minimum.intValue());
    assertEquals(3, tc.values.length);
    assertEquals(2, tc.values[1].intValue());
    assertEquals("dave", tc.strings[1]);
    assertEquals("[this, is, the, time, for, all, good, men]", extra + "");
  }
  @Test public void properties() {
    final TestCommand ____ = new TestCommand();
    extract(new Properties() {
      static final long serialVersionUID = 1L;
      {
        put("input", "inputfile");
        put("o", "outputfile");
        put("someoption", "true");
        put("m", "10");
        put("values", "1:2:3");
        put("strings", "sam;dave;jolly");
      }
    }, ____);
    assertEquals("inputfile", ____.inputFilename);
    assertEquals(new File("outputfile"), ____.outputFile);
    assert ____.someoption;
    assertEquals(10, ____.minimum.intValue());
    assertEquals(3, ____.values.length);
    assertEquals(2, ____.values[1].intValue());
    assertEquals("dave", ____.strings[1]);
  }
  @Test public void propertiesViaSetter() {
    final Object ____ = new Object() {
      @External(name = "option") public void setHashCode(final int hashCode) {
        this.hashCode = hashCode;
      }

      int hashCode = nextInt();

      @Override public int hashCode() {
        return hashCode;
      }
    };
    extract(new Properties() {
      {
        put("option", nextIntS());
      }
      static final long serialVersionUID = 1L;
    }, ____);
    assertEquals(lastInt(), ____.hashCode());
  }
  @Test public void toProperties() {
    assertEquals("value1", Introspector.toProperties(new Object() {
      @External final String key1 = "value1";

      @External public void setKey2(@SuppressWarnings("unused") final String key2) {
        /* Demo */
      }
    }).get("key1"));
  }
  @Test public void toOrderedMapNoObjects() {
    assert Introspector.toOrderedMap() != null;
  }
  @Test public void toOrderedMapTwoObjects() {
    final Map<String, String> m = Introspector.toOrderedMap(new Object() {
      int key = 10;
      @External final int ____1____1 = ++key;
      @External final int ____1____2 = ++key;
      @External final int ____1____3 = ++key;
      @External final int ____1____4 = ++key;
      @External final int ____1____5 = ++key;
    }, new Object() {
      int key = 20;
      @External final int ____2____1 = ++key;
      @External final int ____2____2 = ++key;
      @External final int ____2____3 = ++key;
      @External final int ____2____4 = ++key;
      @External final int ____2____5 = ++key;
    });
    assertEquals(
        "[____1____1, ____1____2, ____1____3, ____1____4, ____1____5, ____2____1, ____2____2, ____2____3, ____2____4, ____2____5]",
        m.keySet() + "");
    assertEquals("[11, 12, 13, 14, 15, 21, 22, 23, 24, 25]", m.values() + "");
  }
  @Test public void toOrderedDuplicateOptions() {
    final Map<String, String> m = Introspector.toOrderedMap(new Object() {
      @External final int option = 100;
    }, new Object() {
      @External final int option = 200;
    });
    assertEquals("[option]", m.keySet() + "");
    assertEquals("[100]", m.values() + "");
  }
  @Test public void toOrderedMapGetterMethods() {
    final Map<String, String> m = Introspector.toOrderedMap(new Object() {
      @External String method1() {
        return "value1";
      }
      @External String method2() {
        return "value2";
      }
      @External String method3() {
        return "value3";
      }
    });
    assertEquals(3, m.size());
    assertEquals("value1", m.get("method1"));
    assertEquals("value2", m.get("method2"));
    assertEquals("value3", m.get("method3"));
  }
  @Test public void useIntMethod() {
    assertEquals(1, Introspector.toOrderedMap(new Object() {
      @Override @External public int hashCode() {
        return 1;
      }
    }).size());
  }
  @Test public void correctIntMethod() {
    assertEquals("122", Introspector.toOrderedMap(new Object() {
      @Override @External public int hashCode() {
        return 122;
      }
    }).get("hashCode"));
  }
  @Test public void ignoreVoidMethods() {
    abstract class ____ {
      public abstract void ________();
    }
    assertEquals(0, Introspector.toOrderedMap(new ____() {
      @Override @External public void ________() {
        notify();
      }
    }).size());
  }
  @Test public void correctMethodName() {
    final Map<String, String> m = Introspector.toOrderedMap(new Object() {
      final int returnedValue = nextInt();

      @External(name = "myName") public int badName() {
        return returnedValue;
      }
    });
    assertEquals(1, m.size());
    assertNull(m.get("badName"));
    assert m.get("myName") != null;
    assertEquals(lastInt() + "", m.get("myName"));
  }
  @Test public void multipleTargets() {
    class ____ {
      @External int anotherOption = nextInt();
    }
    final ____ ____ = new ____();
    extract(args("-option", "EnumB", "-anotherOption", nextIntS()), ClassWithEnumOption.class, ____);
    assertEquals(EnumType.EnumB, ClassWithEnumOption.option);
    assertEquals(____.anotherOption, lastInt());
  }
  @Test public void finalNonStaticIntegerOption() {
    class ____ {
      @External public final Integer i = Integer.valueOf(nextInt());
    }
    final ____ ____ = new ____();
    extract(args("-i", nextIntS()), ____);
    assertEquals(lastInt(), ____.i.intValue());
  }
  @Test public void finalStringFieldCannotBeChanged() {
    class ____ {
      @External private final String s = "final";
    }
    final ____ ____ = new ____();
    extract(args("-s", "changed"), ____);
    assertEquals("final", ____.s);
  }
  @Test public void manyStaticOptions() {
    extract(args("-input", "inputfile", "-output", "outputfile"), TestCommand4.class);
    assertEquals("inputfile", TestCommand4.input);
    assertEquals("outputfile", TestCommand4.output);
  }
  @Test(expected = FieldIsFinal.class) public void staticFinalFieldCannotBeChanged() {
    extract(args("-num", nextIntS()), StaticFinalField.class);
  }

  private SecurityManager oldSecurityManager;

  @Test public void leftOverArguments() {
    final TestCommand2 tc = new TestCommand2();
    final List<String> extra = extract(args( //
        "-input", "inputfile", //
        "-o", "outputfile", //
        "-someoption", //
        "-m", "10", //
        "-values", "1:2:3" //
    ), tc);
    assertEquals(0, extra.size());
    assertEquals("inputfile", tc.getInputFilename());
    assertEquals(new File("outputfile"), tc.getOutputFile());
    assert tc.isSomeoption();
    assertEquals(10, tc.getMinimum().intValue());
    assertEquals(3, tc.getValues().length);
    assertEquals(2, tc.getValues()[1].intValue());
  }
  @Test(expected = NonArray.class) public void residueNonArray() {
    extract(args(0), new Object() {
      @External.Residue File ____;
    });
  }
  @Test public void residueSingleField() {
    class ____ {
      @External.Residue File[] ____;
    }
    final ____ ____ = new ____();
    final int n = 10;
    extract(args(n), ____);
    assertEquals(n, ____.____.length);
  }
  @Test public void residueMultipleFields() {
    class ____ {
      @External.Residue File[] ____1;
      @External.Residue File[] ____2;
    }
    final ____ ____ = new ____();
    final int n = 11;
    extract(args(n), ____);
    assertEquals(n, ____.____1.length);
    assertEquals(n, ____.____2.length);
  }

  static class ResidueStaticField {
    @External.Residue static File[] ____1;
    @External.Residue static File[] ____2;
  }

  @Test public void residueStaticFields() {
    final int n = 12;
    extract(args(n), ResidueStaticField.class);
    assertEquals(n, ResidueStaticField.____1.length);
    assertEquals(n, ResidueStaticField.____2.length);
  }
  @Test public void residueStaticFieldsThroughInstance() {
    final int n = 13;
    extract(args(n), new ResidueStaticField());
    assertEquals(n, ResidueStaticField.____1.length);
    assertEquals(n, ResidueStaticField.____2.length);
  }
  @Test public void residueStaticInheritedFieldsThroughInstance() {
    final int n = 14;
    class ____ extends ResidueStaticField {
      @External.Residue File[] ____3;
    }
    final ____ ____ = new ____();
    extract(args(n), ____);
    assertEquals(n, ResidueStaticField.____1.length);
    assertEquals(n, ResidueStaticField.____2.length);
    assertEquals(n, ____.____3.length);
  }
  @Test public void residueStaticInheritedFieldsThroughInstanceCorrect() {
    class ____ extends ResidueStaticField {
      @External.Residue File[] ____3;
    }
    final ____ ____ = new ____();
    extract(args("/tmp/a", "hello", ".a"), ____);
    assertEquals(new File("/tmp/a"), ResidueStaticField.____1[0]);
    assertEquals(new File("hello"), ResidueStaticField.____2[1]);
    assertEquals(new File(".a"), ____.____3[2]);
  }
  @Test(expected = InvalidEnumValue.class) //
  public void invalidEnumOption() {
    extract(args("-option", nextIntS()), ClassWithEnumOption.class);
  }
  @Test(expected = InvalidEnumValue.class) //
  public void invalidEnumViaProperties() {
    extract(new Properties() {
      {
        put("option", nextIntS());
      }
      static final long serialVersionUID = 1L;
    }, ClassWithEnumOption.class);
  }
  @Test(expected = FieldConversionError.class) //
  public void propertiesSetterThrowsException() {
    extract(new Properties() {
      {
        put("hashCode", nextIntS());
      }
      static final long serialVersionUID = 1L;
    }, new Object() {
      @External public void setHashCode(final int hashCode) {
        throw new RuntimeException(hashCode + "");
      }

      int hashCode = nextInt();

      @Override public int hashCode() {
        return hashCode;
      }
    });
  }
  @Test(expected = FieldConversionError.class) //
  public void argumentsSetterThrowsException() {
    extract(args("-hashCode", nextIntS()), new Object() {
      @External public void setHashCode(final int hashCode) {
        throw new RuntimeException(hashCode + "");
      }

      int hashCode = nextInt();

      @Override public int hashCode() {
        return hashCode;
      }
    });
  }
  @Test public void classWithGetter() {
    class ____ {
      @External int option = nextInt();

      int option() {
        return option;
      }
    }
    final ____ it = new ____();
    extract(args("-option", "197"), it);
    assertEquals(197, it.option());
  }
  @Test public void classWithDerived() {
    class Base {
      @External int option = nextInt();

      int option() {
        return option;
      }
    }
    class Derived extends Base {/** Nothing **/
    }
    final Base base = new Derived();
    extract(args("-option", "1961"), base);
    assertEquals(1961, base.option());
  }
  @Test public void inheritedOption() {
    class Base {
      @External int option = nextInt();

      int option() {
        return option;
      }
    }
    class Derived extends Base {/** Nothing **/
    }
    final Base base = new Derived();
    extract(args("-option", "-154"), base);
    assertEquals(-154, base.option());
    final Derived derived = new Derived();
    extract(args("-option", "8432"), derived);
    assertEquals(8432, derived.option());
  }
  @Test public void inheritedRequiredStaticFields() {
    final List<String> leftOver = extract(args("-output", "outputValue", //
        "-key", "keyValue", //
        "-inheritedkey", "inheritedKeyValue"), InhertingFromRequiredFields.class);
    assertEquals(0, leftOver.size());
    assert RequiredStaticFields.key != null;
    assert RequiredStaticFields.output != null;
    assertEquals("outputValue", RequiredStaticFields.output);
    assertEquals("keyValue", RequiredStaticFields.key);
    assertEquals("inheritedKeyValue", InhertingFromRequiredFields.inheritedkey);
  }
  @Test public void optionInDerivedClass() {
    abstract class Base {
      abstract int option();
    }
    class Derived extends Base {
      @External int option = nextInt();

      @Override int option() {
        return option;
      }
    }
    final Base it = new Derived();
    extract(args("-option", "-18432"), it);
    assertEquals(-18432, it.option());
  }
  @Test public void optionInSubSubClass() {
    abstract class Base {
      abstract int option();
    }
    abstract class Sub extends Base {
      @External int option = nextInt();

      @Override int option() {
        return option;
      }
    }
    class SubSub extends Sub {/****/
    }
    final Base it = new SubSub();
    extract(args("-option", "189432"), it);
    assertEquals(189432, it.option());
  }
  @Test public void optionInConstructorSubSubClass() {
    final int oldValue = 173;
    final int newValue = 43 * oldValue + 19;
    abstract class B0 {
      abstract int option();
      B0(final String[] args) {
        extract(args, this);
        assertEquals(newValue, option());
      }
    }
    abstract class B1 extends B0 {
      B1(final String[] args) {
        super(args);
        assertEquals(newValue, option());
      }
    }
    class B2 extends B1 {
      @External int option = oldValue;

      /**
       * Note: Initialization of fields takes place <b>after</b> call to super
       * class' constructor. Thus, one cannot rely on this constructor to inject
       * values.
       */
      B2(final String[] args) {
        super(args);
        assertEquals(oldValue, option());
      }
      @Override int option() {
        return option;
      }
    }
    /**
     * Unsuspecting subclass, is surprised by the {@link External} injection not
     * taking place.
     */
    class B3 extends B2 {
      B3(final String[] args) {
        super(args);
        assertEquals(oldValue, option());
      }
    }
    assertEquals(oldValue, new B3(args("-option", newValue + "")).option());
  }
  @Test public void settingsContainsKeyValue() {
    final String it = settings(new Object() {
      @External String option = "value";
    });
    assertThat(it, containsString("option"));
    assertThat(it, containsString("value"));
    assertThat(it, containsString("option=value"));
    assertThat(it, containsString("\toption=value\n"));
  }
  @Test public void settingsIsOrdered() {
    assertThat(settings(new Object() {
      @External int This;
      @External int is;
      @External int the;
      @External int time;
      @External int for____all;
      @External int good;
      @External int men;
    }), matches("(?s).*This.*is.*the.*time.*for____all.*good.*men.*"));
  }

  @SuppressWarnings("unused") static class ClassWithProperties {
    @External public void setThis(final int __) {
      // filler property function
    }
    @External public void setIs(final int __) {
      // filler property function
    }
    @External public void setThe(final int __) {
      // filler property function
    }
    @External public void setTime(final int __) {
      // filler property function
    }
    @External public void setFor____all(final int __) {
      // filler property function
    }
    @External public void setGood(final int __) {
      // filler property function
    }
    @External public void setMen(final int __) {
      // filler property function
    }
  }

  @Test public void settingsWithPropertiesIsNotNecessarilyOrdered() {
    final String it = settings(new ClassWithProperties());
    assertThat(it, containsString("this"));
    assertThat(it, containsString("is"));
    assertThat(it, containsString("time"));
    assertThat(it, containsString("for____all"));
    assertThat(it, containsString("good"));
    assertThat(it, containsString("men"));
  }
  @Test public void settingsContainsTargetNamesInOrder() {
    class This {
      @External int is;
      @External int the;
      @External int time;
    }
    class For {
      @External int all;
      @External int good;
      @External int men;
    }
    assertThat(settings(new This(), new For()), matches("(?s).*This:.*is.*the.*time.*For:.*all.*good.*men.*"));
  }
  @Test public void usageEnum() {
    for (final EnumType ¢ : EnumType.values())
      assertThat(usage(ClassWithEnumOption.class), containsString(¢ + ""));
  }
  @Test public void usageOrdinaryField() {
    class ____ {
      @External int a = nextInt();
    }
    assertThat(usage(new ____()), containsString(lastInt() + ""));
  }
  @Ignore @Test public void usageStaticFieldValue() {
    assertThat(usage(StaticField.class), containsString(lastInt() + ""));
  }
  @Test public void usageStaticField() {
    usage(StaticField.class);
    extract(args("-num", nextIntS()), StaticField.class);
    assertEquals(lastInt(), StaticField.num.intValue());
  }
  @Test public void usagePublicStaticField() {
    assertThat(usage(PublicStaticFields.class), containsString("optionNamePublicField"));
    assertThat(usage(PublicStaticFields.class), containsString("stringOption"));
    assertThat(usage(PublicStaticFields.class), containsString(lastInt() + ""));
  }
  @Test public void usageValueFieldFoundInDescription() {
    assertThat(usage(new Object() {
      @External("lorem ipsum") public int a;
    }), containsString("lorem ipsum"));
  }
  @Test public void usageullDescriptionConcatenatesValueAndDescription() {
    assertThat(usage(new Object() {
      @External(value = "lorem ip", description = "sum") public int a;
    }), containsString("lorem ipsum"));
  }
  @Test public void usageProperties() {
    assertThat(usage(new Object() {
      @External public void setInput(@SuppressWarnings("unused") final String input) {
        /* Demo */
      }
    }), containsString("-input"));
  }
  @Test public void usageCorrectOrder() {
    assert usage(new Object() {
      @External int AAA;
      @External int BBB;
      @External int CCC;
      @External int DDD;
      @External int EEE;
    }).matches("(?s).*AAA.*BBB.*CCC.*DDD.*EEE.*");
  }
  @Test public void usagePrivateField() {
    assertThat(usage(PrivateStaticField.class), containsString("optionNamePrivateStaticField"));
  }
  @Test(expected = UnrecognizedOption.class) public void unrecognizedOption() {
    extract(args("-option", nextIntS()), new Object());
  }
  @Test(expected = UnrecognizedOption.class) public void unrecognizedStaticOption() {
    extract(args("-option", nextIntS()), Object.class);
  }
  @Test(expected = FieldInitializationError.class) //
  public void exceptionThrowingInitializationOfField() {
    extract(args("-option", nextIntS()), ExceptionThrowingInitializationOfField.class);
  }

  private static class PrivateStaticFinalIntField {
    private static final int option = new Tester().lastInt();

    public static int option() {
      return option;
    }
  }

  @Test(expected = UnrecognizedOption.class) public void privateStaticFinalIntFieldVanishes() {
    extract(args("-option", nextIntS()), PrivateStaticFinalIntField.class);
  }
  @Test public void privateStaticFinalIntFieldCannotBeChanged() {
    try {
      extract(args("-option", nextIntS()), PrivateStaticFinalIntField.class);
      fail("Should have gotten an exception here");
    } catch (final UnrecognizedOption e) {
      // Error is expected!
    }
    assertEquals(new Tester().lastInt(), PrivateStaticFinalIntField.option());
  }
  @Test(expected = DuplicateOption.class) public void simpleRepeatedOption() {
    extract(args("-option", nextIntS(), "-option", nextIntS()), new Object() {
      @External public String option = nextIntS();
    });
  }
  @Test(expected = NumericParsingError.class) public void numericError() {
    extract(args("-option", "ab" + nextInt()), new Object() {
      @External public int option = nextInt();
    });
  }
  @Test(expected = WrongTarget.class) public void instanceDataMemberThroughClassObject() {
    class LocalClass {
      @External private final int n = 0;
    }
    extract(args("-n", nextIntS()), LocalClass.class);
  }
  @Test(expected = ConstructorWithSingleStringArgumentMissing.class) //
  public void missingStringConstructor() {
    class LocalClass {
      {
        /* Demo */
      }
    }
    extract(args("-option", nextIntS()), new Object() {
      @External public LocalClass option;
    });
  }
  @Test(expected = FieldConversionError.class) public void constructorThrowsException() throws Exception {
    extract(args("-option", "value"), new Object() {
      @External public OptionClass option;
    });
  }
  @Test(expected = MissingValueForOption.class) public void missingValue() {
    extract(args("blah", "-option"), new Object() {
      @External public int option;
    });
  }
  @Test(expected = RequiredOption.class) public void missingRequiredOptionProperties() {
    extract(new Properties(), new Object() {
      @External(required = true) public int option;
    });
  }
  @Test public void usageRequired() {
    assertThat(usage(RequiredStaticFields.class), containsString("mandatory"));
  }
  @Test public void usageArray() {
    assertThat(usage(new Object() {
      @External String[] strings = { "Hello", "World!" };
    }), containsString("Hello, World!"));
  }
  @Test public void usageBoolean() {
    assertThat(usage(new Object() {
      @External boolean ____;
    }), containsString("flag"));
  }
  @Test public void usageProperty() {
    assertThat(usage(new Object() {
      @External public void setOption(@SuppressWarnings("unused") final boolean option) {
        // Empty, just for testing
      }
    }), containsString("option"));
  }
  @Test public void usagePropertyValue() {
    assertThat(usage(new Object() {
      @External public void setOption(@SuppressWarnings("unused") final String option) {
        // Empty, just for testing
      }
    }), containsString("myValue"));
  }
  @Test public void usagePropertyReadThrowsException() {
    final String usage = usage(new Object() {
      @External final String firstOption = "firstValue";
      @External final String secondOption = "secondValue";

      @External public void setOption(@SuppressWarnings("unused") final String option) {
        // Empty, just for testing
      }
    });
    assertThat(usage, containsString("firstOption"));
    assertThat(usage, containsString("secondOption"));
    assertThat(usage, containsString("firstValue"));
    assertThat(usage, containsString("secondValue"));
    // All errors in obtaining usage information are ignored
    assertThat(usage, not(containsString("myValue")));
    assertThat(usage, not(containsString("option")));
  }
  @Test(expected = ArrayIndexOutOfBoundsException.class) public void usageErrorExit0Arguments() {
    System.setSecurityManager(new NoExitSecurityManager(1));
    usageErrorExit();
  }
  @Test(expected = SecurityException.class) public void usageErrorExit1Argument() {
    System.setSecurityManager(new NoExitSecurityManager(1));
    usageErrorExit(new Object());
  }
  @Test(expected = SecurityException.class) public void usageErrorExitString1Argument() {
    System.setSecurityManager(new NoExitSecurityManager(1));
    usageErrorExit("MyUsage", new Object());
  }
  @Test(expected = RequiredOption.class) public void missingRequiredStaticOption() {
    extract(args("blah", "195", "tralalah", "195"), new RequiredStaticFields());
  }
  @Test(expected = RequiredOption.class) public void missingRequiredOptionClassObject() {
    extract(args("-input", "inputfile"), TestCommand4.class);
  }
  @Test(expected = UnrecognizedOption.class) public void unrecognizedOptionClassObject() {
    extract(args("-fred", "inputfile", "-output", "outputfile"), TestCommand4.class);
  }

  static class NoExitSecurityManager extends SecurityManager {
    private final int expectedStatus;
    private final SecurityManager oldSecurityManager;

    public NoExitSecurityManager(final int expectedStatus) {
      oldSecurityManager = System.getSecurityManager();
      this.expectedStatus = expectedStatus;
    }
    @Override public void checkPermission(final Permission __) {
      // allow anything.
    }
    @Override public void checkPermission(final Permission __, final Object context) {
      // allow anything.
    }
    @Override public void checkExit(final int status) {
      super.checkExit(status);
      System.setSecurityManager(oldSecurityManager);
      throw expectedStatus != status ? new RuntimeException() : new SecurityException();
    }
  }

  @Test(expected = SecurityException.class) public void usageErrorExitObjectString1Argument() {
    System.setSecurityManager(new NoExitSecurityManager(1));
    usageErrorExit(new Object(), "MyUsage", new Object());
  }
  @Test(expected = SecurityException.class) public void runDemoNoArguments() {
    System.setSecurityManager(new NoExitSecurityManager(1));
    Demo.main(args());
  }
  @Test public void runDemoWithArguments() {
    Demo.main(args(//
        "all", //
        "-gender", //
        "Female", //
        "-skills", //
        "painting,coooking", //
        "-o", //
        "/tmp/output.txt", //
        "-firstName", //
        "Jane", //
        "-path", //
        "/bin:/usr/bin:/usr/sbin", //
        "final", //
        "these", //
        "arguments", //
        "are", //
        "final", //
        "not", //
        "processed"//
    ));
  }

  static class InhertingFromRequiredFields extends RequiredStaticFields {
    @External(required = true) static String inheritedkey;
  }

  private static class PrivateStaticField {
    @External private static Date optionNamePrivateStaticField = new Date();
  }

  private static class PublicStaticFields {
    @External public static Date optionNamePublicField = new Date();
    @External public static String stringOption = new Tester().lastInt() + "";
  }

  private static class ExceptionThrowingInitializationOfField extends Tester {
    @External public static int option = exceptionThrowing();

    private static int exceptionThrowing() {
      int $ = 0;
      final Random r = new Random();
      for (int ¢ = 100; ¢ >= 0; --¢)
        $ ^= r.nextInt() % ¢;
      return $;
    }
  }

  static class OptionClass {
    @SuppressWarnings("unused") OptionClass(final String __) throws Exception {
      throw new IllegalArgumentException();
    }
  }

  static class RequiredStaticFields {
    @External(required = true) static String key;
    @External(required = true) static String output;
  }

  private enum EnumType {
    EnumA, EnumB, EnumC
  }

  private static class ClassWithEnumOption {
    @External static EnumType option;
  }

  static class StaticField {
    @External static Integer num = Integer.valueOf(new Tester().lastInt());

    @Test public void lastIntInInnerStaticClass() {
      assertEquals(FIRST____LASTINT____VALUE, num.intValue());
    }
  }

  static class StaticFinalField {
    @External static final Integer num = Integer.valueOf(173);
  }

  static class TestCommand {
    @External(name = "input", value = "This is the input file", required = true) String inputFilename;
    @External(name = "output", alias = "o", value = "This is the output file", required = true) File outputFile;
    @External("This option can optionally be set") boolean someoption;
    @External(value = "Minimum", alias = "m") Integer minimum;
    @External(value = "List of values", delimiter = ":") Integer[] values;
    @External(value = "List of strings", delimiter = ";") String[] strings;
    @External("not required") public boolean notRequired;
  }

  static class TestCommand2 {
    private String inputFilename;
    private File outputFile;
    private boolean someoption;
    private Integer minimum = Integer.valueOf(0);
    private Integer[] values = new Integer[10];

    public String getInputFilename() {
      return inputFilename;
    }
    @External(name = "input", value = "This is the input file", required = true) //
    public void setInputFilename(final String inputFilename) {
      this.inputFilename = inputFilename;
    }
    public File getOutputFile() {
      return outputFile;
    }
    @External(name = "output", alias = "o", value = "This is the output file", required = true) //
    public void setOutputFile(final File outputFile) {
      this.outputFile = outputFile;
    }
    public boolean isSomeoption() {
      return someoption;
    }
    @External("This option can optionally be set") public void setSomeoption(final boolean someoption) {
      this.someoption = someoption;
    }
    public Integer getMinimum() {
      return minimum;
    }
    @External(value = "Minimum", alias = "m") //
    public void setMinimum(final Integer minimum) {
      this.minimum = minimum;
    }
    public Integer[] getValues() {
      return values;
    }
    @External(value = "List of values", delimiter = ":") //
    public void setValues(final Integer[] values) {
      this.values = values;
    }
  }

  static class TestCommand4 {
    @External static String input;
    @External(required = true) static String output;
  }

  private static String[] args(final String... ¢) {
    return ¢;
  }
  private String[] args(final int i) {
    final String[] $ = new String[i];
    for (int ¢ = 0; ¢ < i; ++¢)
      $[¢] = "N" + nextLong();
    return $;
  }
}

class Generator {
  public byte nextByte() {
    return lastByte = (byte) inner.nextInt();
  }
  public short nextShort() {
    return lastShort = (short) inner.nextInt();
  }
  public long nextLong() {
    return lastLong = inner.nextLong();
  }
  public int nextInt() {
    return lastInt = inner.nextInt();
  }
  public String nextIntS() {
    return nextInt() + "";
  }
  public float nextFloat() {
    return lastFloat = inner.nextFloat();
  }
  public double nextDouble() {
    return lastDouble = inner.nextDouble();
  }
  public int lastByte() {
    return lastByte;
  }
  public int lastShort() {
    return lastShort;
  }
  public int lastInt() {
    return lastInt;
  }
  public long lastLong() {
    return lastLong;
  }
  public float lastFloat() {
    return lastFloat;
  }
  public double lastDouble() {
    return lastDouble;
  }

  private final Random inner = new Random(0);
  private byte lastByte = nextByte();
  private short lastShort = nextShort();
  private int lastInt = nextInt();
  private long lastLong = nextLong();
  private float lastFloat = nextFloat();
  private double lastDouble = nextDouble();
}

/**
 * Borrowed from
 * <a href="http://piotrga.wordpress.com/2009/03/27/hamcrest-regex-matcher/">
 * Piotr Gabryanczyk's blog</a>
 *
 * @author Yossi Gil
 * @since Nov 2, 2011
 */
class RegexMatcher extends BaseMatcher<String> {
  private final String regex;

  public RegexMatcher(final String regex) {
    this.regex = regex;
  }
  @Override public boolean matches(final Object ¢) {
    return ((String) ¢).matches(regex);
  }
  @Override public void describeTo(final Description ¢) {
    ¢.appendText("matches regex=");
  }
  public static RegexMatcher matches(final String regex) {
    return new RegexMatcher(regex);
  }
}
