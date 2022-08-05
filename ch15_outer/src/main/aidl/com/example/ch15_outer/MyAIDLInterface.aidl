// MyAIDLInterface.aidl
package com.example.ch15_outer;

// Declare any non-default types here with import statements

interface MyAIDLInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    int getMaxDuration();
    void start();
    void stop();
}