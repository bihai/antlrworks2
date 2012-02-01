/*
 *  Copyright (c) 2012 Sam Harwell, Tunnel Vision Laboratories LLC
 *  All rights reserved.
 *
 *  The source code of this document is proprietary work, and is not licensed for
 *  distribution. For information about licensing, contact Sam Harwell at:
 *      sam@tunnelvisionlabs.com
 */
package org.antlr.works.editor.st4.experimental;

import org.antlr.netbeans.editor.text.DocumentSnapshot;
import org.antlr.works.editor.st4.experimental.TemplateParser.groupContext;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 *
 * @author Sam Harwell
 */
public class CurrentTemplateContextData {
    @NonNull
    private final DocumentSnapshot snapshot;
    @NullAllowed
    private final groupContext context;

    public CurrentTemplateContextData(@NonNull DocumentSnapshot snapshot, @NullAllowed groupContext context) {
        this.snapshot = snapshot;
        this.context = context;
    }

    @NonNull
    public DocumentSnapshot getSnapshot() {
        return snapshot;
    }

    @CheckForNull
    public groupContext getContext() {
        return context;
    }

    public String getTemplateName() {
        if (context == null) {
            return null;
        }

        // TODO: return proper template name
        return null;
    }
}
