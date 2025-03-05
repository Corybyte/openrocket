package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

public class Glider  extends Transition implements InsideColorComponent {
    private static final Translator trans = Application.getTranslator();

    private InsideColorComponentHandler insideColorComponentHandler = new InsideColorComponentHandler(this);
    private boolean isFlipped;		// If true, the nose cone is converted to a tail cone

    public  Glider(){
    }
}
