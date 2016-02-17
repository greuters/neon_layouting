/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.form.fields.wizard;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.wizard.AbstractWizardProgressField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class WizardProgressFieldChains {

  private WizardProgressFieldChains() {
  }

  protected abstract static class AbstractWizardProgressChain extends AbstractExtensionChain<IWizardProgressFieldExtension<? extends AbstractWizardProgressField>> {

    public AbstractWizardProgressChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IWizardProgressFieldExtension.class);
    }
  }

  public static class WizardProgressFieldStepActionChain extends AbstractWizardProgressChain {

    public WizardProgressFieldStepActionChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execStepIndex(final int stepIndex) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IWizardProgressFieldExtension<? extends AbstractWizardProgressField> next) {
          next.execStepAction(WizardProgressFieldStepActionChain.this, stepIndex);
        }
      };
      callChain(methodInvocation);
    }
  }
}
