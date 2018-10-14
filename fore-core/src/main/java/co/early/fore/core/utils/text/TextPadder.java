package co.early.fore.core.utils.text;

import co.early.fore.core.Affirm;

import static co.early.fore.core.utils.text.TextPadder.Pad.LEFT;

public class TextPadder {

    public enum Pad {
        LEFT,
        RIGHT;
    }

    public static String padText(String textOriginal, int desiredLength, Pad pad, char paddingCharacter) {

        Affirm.notNull(textOriginal);
        Affirm.notNull(pad);

        int paddingCharactersRequired = desiredLength - textOriginal.length();

        if (paddingCharactersRequired < 0) {
            throw new IllegalArgumentException("textOriginal is already longer than the desiredLength, textOriginal.length():" + textOriginal.length() + " desiredLength:" + desiredLength);
        }

        StringBuilder sb = new StringBuilder();
        for (int ii = 0; ii < paddingCharactersRequired; ii++) {
            sb.append(paddingCharacter);
        }

        if (pad == LEFT) {
            sb.append(textOriginal);
        } else {
            sb.insert(0, textOriginal);
        }

        return sb.toString();
    }

}
