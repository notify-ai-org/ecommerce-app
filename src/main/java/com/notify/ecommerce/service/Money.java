package com.notify.ecommerce.service;

final class Money {

    private Money() {}

    static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
