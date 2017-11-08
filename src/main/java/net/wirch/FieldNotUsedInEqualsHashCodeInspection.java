package net.wirch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Sets;
import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.JavaRecursiveElementWalkingVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.util.PsiModificationTracker;

import static com.siyeh.ig.psiutils.MethodUtils.isEquals;
import static com.siyeh.ig.psiutils.MethodUtils.isHashCode;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;

public class FieldNotUsedInEqualsHashCodeInspection extends BaseJavaLocalInspectionTool {

	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly, @NotNull final LocalInspectionToolSession session) {
		return new JavaElementVisitor() {
			@Override
			public void visitMethod(final PsiMethod method) {
				super.visitMethod(method);
				FieldNotUsedInEqualsHashCodeInspection.this.visitMethod(method, holder);
			}
		};
	}

	private void visitMethod(final PsiMethod method, final ProblemsHolder holder) {
		if (isEquals(method) || isHashCode(method)) {
			final PsiClass containingClass = method.getContainingClass();
			if (containingClass == null) {return;}

			final Set<PsiField> fields = CachedValuesManager.getCachedValue(containingClass, () -> getFields(containingClass));
			final Map<PsiMethod, PsiField> getter = CachedValuesManager.getCachedValue(containingClass, () -> getGetter(fields));

			final Set<PsiField> observed = new HashSet<>();
			//noinspection OverlyComplexAnonymousInnerClass
			method.accept(new JavaRecursiveElementWalkingVisitor() {
				@Override
				public void visitReferenceExpression(final PsiReferenceExpression expression) {
					if (fields.isEmpty()) {return;}
					super.visitReferenceExpression(expression);

					final PsiElement reference = expression.resolve();
					if (reference instanceof PsiField) {
						observed.add((PsiField) reference);
					} else if (reference instanceof PsiMethod) {
						observed.add(getter.get(reference));
					}
				}
			});

			for (final PsiField field : Sets.difference(fields, observed)) {
				if (field.getName() == null) {continue;}
				holder.registerProblem(field.getNameIdentifier(), formatWarning(field.getName(), method.getName()),
									   ProblemHighlightType.GENERIC_ERROR_OR_WARNING, (LocalQuickFix) null);

			}
		}
	}

	private static String formatWarning(final String field, final String method) {
		return String.format("Field '%s' is not used in '%s()' method", field, method);
	}

	private CachedValueProvider.Result<Set<PsiField>> getFields(final PsiClass aClass) {
		final Set<PsiField> fields = stream(aClass.getFields()).filter(this::requiredForEqualsHashCode).collect(toSet());
		return new CachedValueProvider.Result<>(unmodifiableSet(fields), PsiModificationTracker.MODIFICATION_COUNT);
	}

	@SuppressWarnings("RedundantIfStatement")
	private boolean requiredForEqualsHashCode(final PsiVariable psiField) {
		final PsiModifierList modifierList = psiField.getModifierList();
		if (modifierList == null) {return true;}

		if (modifierList.hasModifierProperty(PsiModifier.TRANSIENT)) {return false;}
		if (modifierList.hasModifierProperty(PsiModifier.STATIC)) {return false;}

		// final field with literal initializer: effectively constant
		if (modifierList.hasModifierProperty(PsiModifier.FINAL) && psiField.getInitializer() instanceof PsiLiteralExpression) {
			return false;
		}

		return true;
	}

	private CachedValueProvider.Result<Map<PsiMethod, PsiField>> getGetter(final Iterable<PsiField> fields) {
		final Map<PsiMethod, PsiField> getters = new HashMap<>();
		for (final PsiField field : fields) {
			final PsiMethod getter = PropertyUtil.findGetterForField(field);
			if (getter != null) {
				getters.put(getter, field);
			}
		}
		return new CachedValueProvider.Result<>(unmodifiableMap(getters), PsiModificationTracker.MODIFICATION_COUNT);
	}

}
