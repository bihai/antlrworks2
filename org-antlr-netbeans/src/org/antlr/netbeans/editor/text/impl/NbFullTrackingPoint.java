/*
 * This file is not licensed for distribution in source or binary form.
 */
package org.antlr.netbeans.editor.text.impl;

import org.antlr.netbeans.editor.text.PointTrackingMode;
import org.antlr.netbeans.editor.text.TextVersion;
import org.antlr.netbeans.editor.text.TrackingFidelityMode;
import org.openide.util.Parameters;

/**
 *
 * @author sam
 */
public class NbFullTrackingPoint extends NbTrackingPoint {
    private final TrackingFidelityMode trackingFidelity;

    public NbFullTrackingPoint(NbTextVersion textVersion, int position, PointTrackingMode trackingMode, TrackingFidelityMode trackingFidelity) {
        super(textVersion, position, trackingMode);
        Parameters.notNull("trackingFidelity", trackingFidelity);
        this.trackingFidelity = trackingFidelity;
    }

    @Override
    public TrackingFidelityMode getTrackingFidelity() {
        return trackingFidelity;
    }

    @Override
    public int getPosition(TextVersion version, TextVersion cachedVersion, int cachedPosition) {
        Parameters.notNull("version", version);
        if (version.equals(this.getTextVersion())) {
            return getPosition();
        }

        if (!version.getTextBuffer().equals(this.getTextBuffer())) {
            throw new IllegalArgumentException();
        }

        throw new UnsupportedOperationException("Not implemented yet.");
    }

}