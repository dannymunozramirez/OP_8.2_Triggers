package com.ibm.openpages.rule;

import com.ibm.openpages.api.metadata.ITypeDefinition;
import com.ibm.openpages.api.resource.IGRCObject;
import com.ibm.openpages.api.resource.IResource;
import com.ibm.openpages.api.trigger.events.CreateResourceEvent;
import com.ibm.openpages.api.trigger.ext.DefaultRule;

/**
 * This Rule Class determines if the created resource is a LossEvent.
 * It extends the DefaultRule class.
 *
 * @author dannymunoz
 * @date 2023-06-01
 * @project currency_op
 */
public class CurrencyRule extends DefaultRule {

    @Override
    public boolean isApplicable(CreateResourceEvent event) {
        try {
            IResource resource = event.getResource();

            // Checking if the resource obtained is an object and not a folder
            if (!resource.isFolder()) {

                IGRCObject object = (IGRCObject) resource;

                ITypeDefinition systemName = object.getType();
                String iTypeDefinition = systemName.getName();

                // Checking if it matches with given name
                if (iTypeDefinition.equals("LossEvent")) {

                    return true;

                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
