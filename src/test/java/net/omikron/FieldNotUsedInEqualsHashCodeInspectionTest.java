package net.omikron;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.LanguageLevelModuleExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.siyeh.ig.LightInspectionTestCase;

@SuppressWarnings({"StringBufferReplaceableByString", "StringBufferWithoutInitialCapacity"})
public class FieldNotUsedInEqualsHashCodeInspectionTest extends /*LightCodeInsightFixtureTestCase  */ LightInspectionTestCase {

	public void testNormalFieldNotUsedInEquals_doesWarn() {
		final StringBuilder s = new StringBuilder();

		s.append(" class Test {");
		s.append(" 	private int <warning descr=\"Field 'i' is not used in 'equals()' method\">i</warning> = 0;");
		s.append(" ");
		s.append("	@Override");
		s.append(" 	public boolean equals(final Object o) {");
		s.append(" 		return true;");
		s.append(" 	}");
		s.append(" }");

		doTest(s.toString());
	}

	public void testNormalFieldNotUsedInHashCode_doesWarn() {
		final StringBuilder s = new StringBuilder();

		s.append(" class Test {");
		s.append(" 	private int <warning descr=\"Field 'i' is not used in 'hashCode()' method\">i</warning> = 0;");
		s.append(" ");
		s.append("	@Override");
		s.append(" 	public int hashCode() {");
		s.append(" 		return 0;");
		s.append(" 	}");
		s.append(" }");

		doTest(s.toString());
	}

	public void testNormalFieldNotUsedInEqualsAndHashCode_doesWarnForBothMethods() {
		final StringBuilder s = new StringBuilder();

		s.append(" class Test {");
		s.append(" 	private int ")
		 .append("<warning descr=\"Field 'i' is not used in 'hashCode()' method\">")
		 .append("<warning descr=\"Field 'i' is not used in 'equals()' method\">")
		 .append("i")
		 .append("</warning>")
		 .append("</warning>")
		 .append(" = 0;");
		s.append(" ");
		s.append("	@Override");
		s.append(" 	public int hashCode() {");
		s.append(" 		return 0;");
		s.append(" 	}");
		s.append("");
		s.append("	@Override");
		s.append(" 	public boolean equals(final Object o) {");
		s.append(" 		return true;");
		s.append(" 	}");
		s.append(" }");

		doTest(s.toString());
	}

	public void testEqualsAndHashCodeAreMissing_doesNotWarn() {
		final StringBuilder s = new StringBuilder();

		s.append(" class Test {");
		s.append(" 	private int i = 0;");
		s.append(" ");
		s.append(" 	public boolean someMethod(final Object o) {");
		s.append(" 		return true;");
		s.append(" 	}");
		s.append(" }");

		doTest(s.toString());
	}

	public void testIfFieldsAreMissing_doesNotWarn() {
		final StringBuilder s = new StringBuilder();

		s.append(" class Test {");
		s.append(" ");
		s.append("	@Override");
		s.append(" 	public boolean equals(final Object o) {");
		s.append(" 		return true;");
		s.append(" 	}");
		s.append(" ");
		s.append("	@Override");
		s.append(" 	public int hashCode() {");
		s.append(" 		return 0;");
		s.append(" 	}");
		s.append(" }");

		doTest(s.toString());
	}

	public void testEqualsAndHashCodeCorrectlyImplemented_doesNotWarn() {
		final StringBuilder s = new StringBuilder();

		s.append("class Test {");
		s.append("	private int i = 0;");
		s.append("	private int j = 0;");
		s.append("");
		s.append("	@Override");
		s.append("	public boolean equals(final Object o) {");
		s.append("		final Test other = (Test)o;");
		s.append("		return i == other.i && j == other.j;");
		s.append("	}");
		s.append("");
		s.append("	@Override");
		s.append("	public int hashCode() {");
		s.append("		return i * j;");
		s.append("	}");
		s.append("}");

		doTest(s.toString());
	}

	public void testEqualsCorrectlyImplementedUsingObjectsEquals_doesNotWarn() {
		final StringBuilder s = new StringBuilder();

		s.append("import java.util.Objects;");
		s.append("");
		s.append("class Test {");
		s.append("	private int i = 0;");
		s.append("	private int j = 0;");
		s.append("");
		s.append("	@Override");
		s.append("	public boolean equals(final Object o) {");
		s.append("		final Test other = (Test)o;");
		s.append("		return Objects.equals(i, other.i) && Objects.equals(j, other.j);");
		s.append("	}");
		s.append("}");

		doTest(s.toString());
	}

	public void testEqualsCorrectlyImplementedUsingEqualsBuilderAndGetter_doesNotWarn() {
		final StringBuilder s = new StringBuilder();

		s.append("import java.util.Objects;");
		s.append("");
		s.append("class Test {");
		s.append("	private int i = 0;");
		s.append("");
		s.append("	public int getI() {");
		s.append("		return i;");
		s.append("	}");
		s.append("");
		s.append("	class EqualsBuilder {");
		s.append("		EqualsChecker append(Object a, Object b) {");
		s.append("			return new EqualsChecker();");
		s.append("		}");
		s.append("		");
		s.append("		class EqualsChecker {");
		s.append("			boolean isEquals() {");
		s.append("				return true;");
		s.append("			}");
		s.append("		}");
		s.append("	}");
		s.append("");
		s.append("	@Override");
		s.append("	public boolean equals(final Object o) {");
		s.append("		final Test other = (Test)o;");
		s.append("		return new EqualsBuilder().append(getI(), other.getI()).isEquals();");
		s.append("	}");
		s.append("}");

		doTest(s.toString());
	}

	public void testEqualsAndHashCodeCorrectlyImplementedUsingGetters_doesNotWarn() {
		final StringBuilder s = new StringBuilder();

		s.append("class Test {");
		s.append("	static class Parameter {};");
		s.append("");
		s.append("	private Parameter parameter;");
		s.append("	private int j = 0;");
		s.append("");
		s.append("	public Parameter getParameter() {");
		s.append("		return parameter;");
		s.append("	}");
		s.append("");
		s.append("	public int getJ() {");
		s.append("		return j;");
		s.append("	}");
		s.append("");
		s.append("	@Override");
		s.append("	public boolean equals(final Object o) {");
		s.append("		final Test other = (Test)o;");
		s.append("		return getParameter() == other.getParameter() && this.getJ() == other.j;");
		s.append("	}");
		s.append("");
		s.append("	@Override");
		s.append("	public int hashCode() {");
		s.append("		return getParameter().hashCode() * getJ();");
		s.append("	}");
		s.append("}");

		doTest(s.toString());
	}

	public void testEqualsAndHashCodeCorrectlyImplementedUsingBooleanGetters_doesNotWarn() {
		final StringBuilder s = new StringBuilder();

		s.append("class Test {");
		s.append("	private boolean applied = true;");
		s.append("");
		s.append("	public boolean isApplied() {");
		s.append("		return applied;");
		s.append("	}");
		s.append("");
		s.append("	@Override");
		s.append("	public boolean equals(final Object o) {");
		s.append("		final Test other = (Test)o;");
		s.append("		return isApplied() == other.isApplied();");
		s.append("	}");
		s.append("");
		s.append("	@Override");
		s.append("	public int hashCode() {");
		s.append("		return isApplied()?1:0;");
		s.append("	}");
		s.append("}");

		doTest(s.toString());
	}

	public void testOneFieldIsMissingInEquals_doesWarn() {
		final StringBuilder s = new StringBuilder();

		s.append("class Test {");
		s.append("	private String name;");
		s.append("	private String ")
		 .append("<warning descr=\"Field 'address' is not used in 'equals()' method\">")
		 .append("address")
		 .append("</warning>")
		 .append(";");
		s.append("");
		s.append("	@Override");
		s.append("	public boolean equals(final Object o) {");
		s.append("		return name.equals(((Test)o).name);");
		s.append("	}");
		s.append("}");

		doTest(s.toString());
	}

	public void testOneFieldIsMissingInHashCode_doesWarn() {
		final StringBuilder s = new StringBuilder();

		s.append("class Test {");
		s.append("	private String name;");
		s.append("	private String ")
		 .append("<warning descr=\"Field 'address' is not used in 'hashCode()' method\">")
		 .append("address")
		 .append("</warning>")
		 .append(";");
		s.append("");
		s.append("	@Override");
		s.append("	public int hashCode() {");
		s.append("		return name.hashCode();");
		s.append("	}");
		s.append("}");

		doTest(s.toString());
	}

	public void testOneTransientFieldIsMissingInEqualsAndHashCode_doesNotWarn() {
		final StringBuilder s = new StringBuilder();

		s.append("class Test {");
		s.append("	private String name;");
		s.append("	private transient String address;");
		s.append("");
		s.append("	@Override");
		s.append("	public boolean equals(final Object o) {");
		s.append("		return name.equals(((Test)o).name);");
		s.append("	}");
		s.append("");
		s.append("	@Override");
		s.append("	public int hashCode() {");
		s.append("		return name.hashCode();");
		s.append("	}");
		s.append("}");

		doTest(s.toString());
	}

	public void testOneStaticFieldIsMissingInEqualsAndHashCode_doesNotWarn() {
		final StringBuilder s = new StringBuilder();

		s.append("class Test {");
		s.append("	private String name;");
		s.append("	private static String address;");
		s.append("");
		s.append("	@Override");
		s.append("	public boolean equals(final Object o) {");
		s.append("		return name.equals(((Test)o).name);");
		s.append("	}");
		s.append("");
		s.append("	@Override");
		s.append("	public int hashCode() {");
		s.append("		return name.hashCode();");
		s.append("	}");
		s.append("}");

		doTest(s.toString());
	}

	public void testOneFinalFieldWithLiteralInitializerIsMissingInEqualsAndHashCode_doesNotWarn() {
		final StringBuilder s = new StringBuilder();

		s.append("class Test {");
		s.append("	private String name;");
		s.append("	private final String address = \"\";");
		s.append("");
		s.append("	@Override");
		s.append("	public boolean equals(final Object o) {");
		s.append("		return name.equals(((Test)o).name);");
		s.append("	}");
		s.append("");
		s.append("	@Override");
		s.append("	public int hashCode() {");
		s.append("		return name.hashCode();");
		s.append("	}");
		s.append("}");

		doTest(s.toString());
	}

	public void testOneFinalFieldWithDynamicInitializerIsMissingInEqualsAndHashCode_doesWarn() {
		final StringBuilder s = new StringBuilder();

		s.append("class Test {");
		s.append("	private String name;");
		s.append("	private final String " //
									 + "<warning descr=\"Field 'address' is not used in 'equals()' method\">" //
									 + "<warning descr=\"Field 'address' is not used in 'hashCode()' method\">" //
									 + "address" //
									 + "</warning>" //
									 + "</warning>" //
									 + " = getAddress();");
		s.append("");
		s.append("	private String getAddress() {return \"\";}");
		s.append("");
		s.append("	@Override");
		s.append("	public boolean equals(final Object o) {");
		s.append("		return name.equals(((Test)o).name);");
		s.append("	}");
		s.append("");
		s.append("	@Override");
		s.append("	public int hashCode() {");
		s.append("		return name.hashCode();");
		s.append("	}");
		s.append("}");

		doTest(s.toString());
	}

	public void testOneFinalFieldWithConstructorInitializationIsMissingInEqualsAndHashCode_doesWarn() {
		final StringBuilder s = new StringBuilder();

		s.append("class Test {");
		s.append("	private String name;");
		s.append("	private final String " //
									 + "<warning descr=\"Field 'address' is not used in 'equals()' method\">" //
									 + "<warning descr=\"Field 'address' is not used in 'hashCode()' method\">" //
									 + "address" //
									 + "</warning>" //
									 + "</warning>" //
									 + ";");
		s.append("");
		s.append("	public Test(final String address) {");
		s.append("		this.address = address;");
		s.append("	}");
		s.append("");
		s.append("	@Override");
		s.append("	public boolean equals(final Object o) {");
		s.append("		return name.equals(((Test)o).name);");
		s.append("	}");
		s.append("");
		s.append("	@Override");
		s.append("	public int hashCode() {");
		s.append("		return name.hashCode();");
		s.append("	}");
		s.append("}");

		doTest(s.toString());
	}

	public void testNormalFieldNotUsedInEqualsAndHashCodeSuppressedByFieldAnnotation_doesNotWarn() {
		final StringBuilder s = new StringBuilder();

		s.append("class Test {");
		s.append("	@SuppressWarnings(\"FieldNotUsedInEqualsHashCode\")");
		s.append("	private int i = 0;");
		s.append(" ");
		s.append("	@Override");
		s.append("	public int hashCode() {");
		s.append("		return 0;");
		s.append("	}");
		s.append("");
		s.append("	@Override");
		s.append("	public boolean equals(final Object o) {");
		s.append("		return true;");
		s.append("	}");
		s.append("}");

		doTest(s.toString());
	}

	public void testNormalFieldNotUsedInEqualsAndHashCodeSuppressedByClassAnnotation_doesNotWarn() {
		final StringBuilder s = new StringBuilder();

		s.append("@SuppressWarnings(\"FieldNotUsedInEqualsHashCode\")");
		s.append("class Test {");
		s.append("	private int i = 0;");
		s.append(" ");
		s.append("	@Override");
		s.append("	public int hashCode() {");
		s.append("		return 0;");
		s.append("	}");
		s.append("");
		s.append("	@Override");
		s.append("	public boolean equals(final Object o) {");
		s.append("		return true;");
		s.append("	}");
		s.append("}");

		doTest(s.toString());
	}

	@Nullable
	@Override
	protected InspectionProfileEntry getInspection() {
		return new FieldNotUsedInEqualsHashCodeInspection();
	}

	@Override
	@NotNull
	protected LightProjectDescriptor getProjectDescriptor() {
		return new DefaultLightProjectDescriptor() {
			@Override
			public Sdk getSdk() {
				return JavaSdk.getInstance().createJdk("jdk-1.8", "C:\\Program Files\\Java\\jdk1.8.0_112\\jre");
			}

			@Override
			public void configureModule(@NotNull final Module module, @NotNull final ModifiableRootModel model, @NotNull final ContentEntry contentEntry) {
				model.getModuleExtension(LanguageLevelModuleExtension.class).setLanguageLevel(LanguageLevel.JDK_1_8);
			}

		};
	}

}
