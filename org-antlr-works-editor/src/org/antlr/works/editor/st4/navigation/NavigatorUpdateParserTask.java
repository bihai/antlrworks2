/*
 * [The "BSD license"]
 *  Copyright (c) 2012 Sam Harwell
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.antlr.works.editor.st4.navigation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.antlr.netbeans.editor.navigation.Description;
import org.antlr.netbeans.editor.text.DocumentSnapshot;
import org.antlr.netbeans.editor.text.VersionedDocument;
import org.antlr.netbeans.editor.text.VersionedDocumentUtilities;
import org.antlr.netbeans.parsing.spi.ParserData;
import org.antlr.netbeans.parsing.spi.ParserDataDefinition;
import org.antlr.netbeans.parsing.spi.ParserDataOptions;
import org.antlr.netbeans.parsing.spi.ParserResultHandler;
import org.antlr.netbeans.parsing.spi.ParserTask;
import org.antlr.netbeans.parsing.spi.ParserTaskDefinition;
import org.antlr.netbeans.parsing.spi.ParserTaskManager;
import org.antlr.netbeans.parsing.spi.ParserTaskProvider;
import org.antlr.netbeans.parsing.spi.ParserTaskScheduler;
import org.antlr.works.editor.st4.StringTemplateEditorKit;
import org.antlr.works.editor.st4.TemplateParserDataDefinitions;
import org.antlr.works.editor.st4.experimental.CurrentTemplateContextData;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeRegistration;

/**
 *
 * @author Sam Harwell
 */
public class NavigatorUpdateParserTask implements ParserTask {
    private static final NavigatorUpdateParserTask INSTANCE = new NavigatorUpdateParserTask();

    private NavigatorUpdateParserTask() {
    }

    @Override
    public ParserTaskDefinition getDefinition() {
        return Definition.INSTANCE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void parse(ParserTaskManager taskManager, JTextComponent component, DocumentSnapshot snapshot, Collection<ParserDataDefinition<?>> requestedData, ParserResultHandler results)
        throws InterruptedException, ExecutionException {

        JTextComponent currentComponent = EditorRegistry.lastFocusedComponent();
        if (currentComponent == null) {
            return;
        }

        Document document = currentComponent.getDocument();
        if (document == null || !VersionedDocumentUtilities.getVersionedDocument(document).equals(snapshot.getVersionedDocument())) {
            return;
        }

        Future<ParserData<Description>> futureData = taskManager.getData(snapshot, TemplateParserDataDefinitions.NAVIGATOR_ROOT, EnumSet.of(ParserDataOptions.NO_UPDATE));
        ParserData<Description> parserData = futureData.get();
        if (parserData == null) {
            return;
        }

        Description root = parserData.getData();

        Future<ParserData<CurrentTemplateContextData>> futureContextData = taskManager.getData(snapshot, TemplateParserDataDefinitions.CURRENT_TEMPLATE_CONTEXT);
        ParserData<CurrentTemplateContextData> parserContextData = futureContextData.get();
        CurrentTemplateContextData context = null;
        if (parserContextData != null) {
            context = parserContextData.getData();
        }

        String selectedRule = context != null ? context.getTemplateName() : null;
        TemplatesPanelUI ui = TemplatesPanel.findTemplatesPanelUI();
        ui.refresh(root, selectedRule);
    }

    private static final class Definition implements ParserTaskDefinition {
        public static final Definition INSTANCE = new Definition();

        private static final Collection<ParserDataDefinition<?>> INPUTS =
            Arrays.<ParserDataDefinition<?>>asList(
                TemplateParserDataDefinitions.NAVIGATOR_ROOT,
                TemplateParserDataDefinitions.CURRENT_TEMPLATE_CONTEXT);

        private static final Collection<ParserDataDefinition<?>> OUTPUTS =
            Collections.<ParserDataDefinition<?>>emptyList();

        @Override
        public Collection<ParserDataDefinition<?>> getInputs() {
            return INPUTS;
        }

        @Override
        public Collection<ParserDataDefinition<?>> getOutputs() {
            return OUTPUTS;
        }

        @Override
        public Class<? extends ParserTaskScheduler> getScheduler() {
            return ParserTaskScheduler.INPUT_SENSITIVE_TASK_SCHEDULER;
        }
    }

    @MimeRegistration(mimeType=StringTemplateEditorKit.TEMPLATE_MIME_TYPE, service=ParserTaskProvider.class)
    public static final class Provider implements ParserTaskProvider {

        @Override
        public ParserTaskDefinition getDefinition() {
            return Definition.INSTANCE;
        }

        @Override
        public ParserTask createTask(VersionedDocument document) {
            return NavigatorUpdateParserTask.INSTANCE;
        }

    }

}
