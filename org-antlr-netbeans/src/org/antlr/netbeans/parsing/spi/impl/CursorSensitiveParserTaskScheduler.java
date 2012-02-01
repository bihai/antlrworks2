/*
 *  Copyright (c) 2012 Sam Harwell, Tunnel Vision Laboratories LLC
 *  All rights reserved.
 *
 *  The source code of this document is proprietary work, and is not licensed for
 *  distribution. For information about licensing, contact Sam Harwell at:
 *      sam@tunnelvisionlabs.com
 */
package org.antlr.netbeans.parsing.spi.impl;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.antlr.netbeans.editor.text.DocumentSnapshot;
import org.antlr.netbeans.editor.text.SnapshotPosition;
import org.antlr.netbeans.editor.text.VersionedDocument;
import org.antlr.netbeans.parsing.spi.ParseContext;
import org.antlr.netbeans.parsing.spi.ParserTaskScheduler;
import org.openide.util.lookup.ServiceProvider;

/**
 * A task scheduler which schedules tasks when the active editor window changes, the
 * content of the active document changes, and/or the caret position changes within
 * the active document.
 *
 * @author Sam Harwell
 */
@ServiceProvider(service=ParserTaskScheduler.class)
public class CursorSensitiveParserTaskScheduler extends CurrentDocumentParserTaskScheduler {

    private JTextComponent currentEditor;
    private CaretListener caretListener;

    @Override
    protected void setEditor(JTextComponent editor) {
        if (currentEditor != null) {
            currentEditor.removeCaretListener(caretListener);
        }

        super.setEditor(editor);
        currentEditor = editor;

        if (editor != null) {
            if (caretListener == null) {
                caretListener = new CaretListenerImpl();
            }

            editor.addCaretListener(caretListener);
        }
    }

    @Override
    protected ParseContext createParseContext(VersionedDocument versionedDocument, JTextComponent editor) {
        Caret caret = editor.getCaret();
        int offset = caret.getDot();

        SnapshotPosition position = new SnapshotPosition(versionedDocument.getCurrentSnapshot(), offset);
        return new ParseContext(this, position, editor);
    }

    private class CaretListenerImpl implements CaretListener {

        @Override
        public void caretUpdate(CaretEvent e) {
            DocumentSnapshot snapshot = versionedDocument.getCurrentSnapshot();
            SnapshotPosition position = new SnapshotPosition(snapshot, e.getDot());
            ParseContext context = new ParseContext(CursorSensitiveParserTaskScheduler.this, position, currentEditor);
            schedule(context);
        }
    }

}
