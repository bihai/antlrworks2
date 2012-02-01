/*
 *  Copyright (c) 2012 Sam Harwell, Tunnel Vision Laboratories LLC
 *  All rights reserved.
 *
 *  The source code of this document is proprietary work, and is not licensed for
 *  distribution. For information about licensing, contact Sam Harwell at:
 *      sam@tunnelvisionlabs.com
 */
package org.antlr.works.editor.st4.highlighter;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;

public class OutsideHighlighterLexer extends OutsideHighlighterLexerBase {
    private final TemplateHighlighterLexer aggregateLexer;

    public OutsideHighlighterLexer(CharStream input, TemplateHighlighterLexer lexer) {
        super(input);
        aggregateLexer = lexer;
    }

    public TemplateHighlighterLexer getAggregateLexer() {
        return aggregateLexer;
    }

    public TemplateLexerMode getMode() {
        return getAggregateLexer().getMode();
    }

    @Override
    public OutermostTemplate getOutermost() {
        return getAggregateLexer().getOutermost();
    }

    @Override
    public int getAnonymousTemplateLevel() {
        return getAggregateLexer().getAnonymousTemplateLevel();
    }

    @Override
    public char getOpenDelimiter() {
        return getAggregateLexer().getOpenDelimiter();
    }

    @Override
    public char getCloseDelimiter() {
        return getAggregateLexer().getCloseDelimiter();
    }

    public boolean getInComment() {
        return getAggregateLexer().getInComment();
    }

    public void setInComment(boolean value) {
        getAggregateLexer().setInComment(value);
    }

    @Override
    public Token nextToken() {
        Token token = super.nextToken();
        switch (token.getType()) {
        case CONTINUE_COMMENT:
            setInComment(true);
            token.setType(COMMENT);
            break;

        case END_COMMENT:
            setInComment(false);
            token.setType(COMMENT);
            break;

        default:
            break;
        }

        return token;
    }

    @Override
    public void mTokens() throws RecognitionException {
        if (input.LA(1) == '\r' || input.LA(1) == '\n') {
            super.mTokens();
            return;
        }

        if (getInComment()) {
            mCONTINUE_COMMENT();
        } else {
            super.mTokens();
        }
    }
}
