package com.jit.automation.ios.utils;

public class ChoiceSelector 
{
	private static final ChoiceSelector DONE = new ChoiceSelector(null) {
        @Override
        public ChoiceSelector when(String subString, Runnable r) {
            return this;
        }

        @Override
        public void orElse(Runnable r) {
        }
    };

    private final String str;

    private ChoiceSelector(String str) {
        this.str = str;
    }

    public ChoiceSelector when(String subString, Runnable r) {
        if(str.toLowerCase().contains(subString.toLowerCase())) {
            r.run();
            return DONE;
        }
        return this;
    }

    public void orElse(Runnable r) {
        r.run();
    }

    public static ChoiceSelector of(String str) {
        return new ChoiceSelector(str);
    }
}
