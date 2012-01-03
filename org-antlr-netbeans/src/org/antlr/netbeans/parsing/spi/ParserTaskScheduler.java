/*
 * [The "BSD license"]
 *  Copyright (c) 2011 Sam Harwell
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
package org.antlr.netbeans.parsing.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.text.JTextComponent;
import org.antlr.netbeans.editor.text.VersionedDocument;
import org.antlr.netbeans.parsing.spi.impl.CurrentDocumentParserTaskScheduler;
import org.antlr.netbeans.parsing.spi.impl.CursorSensitiveParserTaskScheduler;
import org.antlr.netbeans.parsing.spi.impl.DataInputParserTaskScheduler;
import org.antlr.netbeans.parsing.spi.impl.DocumentContentParserTaskScheduler;
import org.antlr.netbeans.parsing.spi.impl.SelectedNodesParserTaskScheduler;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.openide.util.Lookup;

/**
 *
 * @author Sam Harwell
 */
public abstract class ParserTaskScheduler {

    public static final Class<? extends ParserTaskScheduler> CONTENT_SENSITIVE_TASK_SCHEDULER =
        DocumentContentParserTaskScheduler.class;

    public static final Class<? extends ParserTaskScheduler> EDITOR_SENSITIVE_TASK_SCHEDULER =
        CurrentDocumentParserTaskScheduler.class;

    public static final Class<? extends ParserTaskScheduler> CURSOR_SENSITIVE_TASK_SCHEDULER =
        CursorSensitiveParserTaskScheduler.class;

    public static final Class<? extends ParserTaskScheduler> SELECTED_NODES_SENSITIVE_TASK_SCHEDULER =
        SelectedNodesParserTaskScheduler.class;

    public static final Class<? extends ParserTaskScheduler> INPUT_SENSITIVE_TASK_SCHEDULER =
        DataInputParserTaskScheduler.class;

    private final Map<VersionedDocument, Collection<ScheduledFuture<ParserData<?>>>> scheduledDocumentDataTasks =
        new WeakHashMap<VersionedDocument, Collection<ScheduledFuture<ParserData<?>>>>();

    private final Map<VersionedDocument, Collection<ScheduledFuture<Collection<ParserData<?>>>>> scheduledDocumentTasks =
        new WeakHashMap<VersionedDocument, Collection<ScheduledFuture<Collection<ParserData<?>>>>>();

    private boolean initialized;

    public final void initialize() {
        if (initialized) {
            throw new IllegalStateException("The scheduler is already initialized.");
        }

        initializeImpl();
    }

    protected void schedule(VersionedDocument document) {
        schedule(document, (JTextComponent)null);
    }

    protected void schedule(VersionedDocument document, JTextComponent component) {
        schedule(document, component, getParseDelayMilliseconds(), TimeUnit.MILLISECONDS);
    }

    protected void schedule(VersionedDocument document, JTextComponent component, int delay, TimeUnit timeUnit) {
        if (document == null) {
            return;
        }

        Collection<ScheduledFuture<ParserData<?>>> existing;
        synchronized(scheduledDocumentDataTasks) {
            existing = scheduledDocumentDataTasks.get(document);
            if (existing == null) {
                existing = new ArrayList<ScheduledFuture<ParserData<?>>>();
                scheduledDocumentDataTasks.put(document, existing);
            }
        }

        synchronized (existing) {
            for (ScheduledFuture<ParserData<?>> future : existing) {
                future.cancel(false);
            }

            existing.clear();
        }

        // Schedule data updates
        @SuppressWarnings("unchecked")
        Collection<? extends ParserDataDefinition<?>> mimeData = (Collection<? extends ParserDataDefinition<?>>)MimeLookup.getLookup(document.getMimeType()).lookupAll(ParserDataDefinition.class);
        Set<ParserDataDefinition<?>> currentScheduledData = new HashSet<ParserDataDefinition<?>>();
        for (ParserDataDefinition<?> data : mimeData) {
            if (getClass().equals(data.getScheduler())) {
                currentScheduledData.add(data);
            }
        }

        Collection<ScheduledFuture<ParserData<?>>> futures = getTaskManager().scheduleData(document, component, currentScheduledData, delay, timeUnit);
        synchronized (existing) {
            existing.addAll(futures);
        }
    }

    protected void schedule(VersionedDocument document, Collection<ParserTaskProvider> tasks) {
        schedule(document, null, tasks);
    }

    protected void schedule(VersionedDocument document, JTextComponent component, Collection<ParserTaskProvider> tasks) {
        schedule(document, component, tasks, getParseDelayMilliseconds(), TimeUnit.MILLISECONDS);
    }

    protected void schedule(VersionedDocument document, JTextComponent component, Collection<? extends ParserTaskProvider> taskProviders, int delay, TimeUnit timeUnit) {
        if (document == null) {
            return;
        }

        Collection<ScheduledFuture<Collection<ParserData<?>>>> existing;
        synchronized(scheduledDocumentTasks) {
            existing = scheduledDocumentTasks.get(document);
            if (existing == null) {
                existing = new ArrayList<ScheduledFuture<Collection<ParserData<?>>>>();
                scheduledDocumentTasks.put(document, existing);
            }
        }

        synchronized (existing) {
            for (ScheduledFuture<Collection<ParserData<?>>> future : existing) {
                future.cancel(false);
            }

            existing.clear();
        }

        // Schedule task updates
        Set<ParserTaskProvider> currentScheduledProviders = new HashSet<ParserTaskProvider>();
        providerLoop:
        for (ParserTaskProvider provider : taskProviders) {
            if (getClass().equals(provider.getDefinition().getScheduler())) {
                currentScheduledProviders.add(provider);
            }
        }

        Collection<ScheduledFuture<Collection<ParserData<?>>>> futures = getTaskManager().schedule(document, component, currentScheduledProviders, delay, timeUnit);
        synchronized (existing) {
            existing.addAll(futures);
        }
    }
    
    protected int getParseDelayMilliseconds() {
        return 500;
    }

    protected ParserTaskManager getTaskManager() {
        return Lookup.getDefault().lookup(ParserTaskManager.class);
    }

    protected void initializeImpl() {
    }
}