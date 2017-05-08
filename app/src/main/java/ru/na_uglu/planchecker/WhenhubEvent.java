package ru.na_uglu.planchecker;


class WhenhubEvent {
    String title;
    String dateTime;
    int customField;

    WhenhubEvent(String title, String dateTime, int customField) {
        this.title = title;
        this.customField = customField;
        this.dateTime = dateTime;
    }

}
