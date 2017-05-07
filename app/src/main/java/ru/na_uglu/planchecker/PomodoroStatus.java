package ru.na_uglu.planchecker;


enum PomodoroStatus {
    notActive("not_active"),
    active("active"),
    paused("paused"),
    ended("ended"),
    flow("flow"),
    relax("relax");

    private String stringValue;

    PomodoroStatus(String someValue) {
        stringValue = someValue;
    }

    @Override
    public String toString() {
        return stringValue;
    }

    public static PomodoroStatus getStatusFromString(String str) {
        PomodoroStatus pomodoroStatus;
        if (str.equals(notActive.toString())) {
            pomodoroStatus = notActive;
        } else if (str.equals(active.toString())) {
            pomodoroStatus = active;
        } else if (str.equals(paused.toString())) {
            pomodoroStatus = paused;
        } else if (str.equals(ended.toString())) {
            pomodoroStatus = ended;
        } else if (str.equals(flow.toString())) {
            pomodoroStatus = flow;
        } else {
            pomodoroStatus = relax;
        }
        return pomodoroStatus;
    }

    public boolean isTimerActive() {
        if (stringValue.equals(active.toString()) || stringValue.equals(flow.toString())) {
            return true;
        } else {
            return false;
        }
    }
}
