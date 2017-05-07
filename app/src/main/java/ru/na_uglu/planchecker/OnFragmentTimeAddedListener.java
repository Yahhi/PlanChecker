package ru.na_uglu.planchecker;

interface OnFragmentTimeAddedListener {
    int getTaskIdentifier();
    void onTimeAddedInteraction(int timeInMinutes);
}
