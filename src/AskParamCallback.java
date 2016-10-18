package ru.askor.blagosfera.interfaces;

/**
 * @author Shatalin Dmitry
 * @version v54
 */
public interface AskParamCallback<P, R> {
    R callbackFunc(P params);
};