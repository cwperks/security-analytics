/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */
package org.opensearch.securityanalytics.commons.aws;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * This plugin uses aws libraries to connect to STS. For these remote calls the plugin needs
 * [SocketPermission] 'connect' to establish connections. This class wraps the operations requiring access in
 * [AccessController.doPrivileged] blocks.
 */
public class SocketAccess {
    public static <T> T doPrivileged(PrivilegedAction<T> operation) {
        SpecialPermission.check();
        return AccessController.doPrivileged(operation);
    }

    public static <T> T doPrivilegedIOException(PrivilegedExceptionAction<T> operation) throws IOException {
        SpecialPermission.check();
        try {
            return AccessController.doPrivileged(operation);
        } catch (PrivilegedActionException e) {
            throw (IOException) e.getCause();
        }
    }

    public static void doPrivilegedVoid(Runnable action) {
        SpecialPermission.check();
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            action.run();
            return null;
        });
    }
}


//import org.opensearch.SpecialPermission
//import java.io.IOException
//import java.security.AccessController
//import java.security.PrivilegedAction
//import java.security.PrivilegedActionException
//import java.security.PrivilegedExceptionAction
//
///**
// * This plugin uses aws libraries to connect to STS. For these remote calls the plugin needs
// * [SocketPermission] 'connect' to establish connections. This class wraps the operations requiring access in
// * [AccessController.doPrivileged] blocks.
// */
//internal object SocketAccess {
//    @JvmStatic
//    fun <T> doPrivileged(operation: PrivilegedAction<T>?): T {
//        SpecialPermission.check()
//        return AccessController.doPrivileged(operation)
//    }
//
//    @Throws(IOException::class)
//    fun <T> doPrivilegedIOException(operation: PrivilegedExceptionAction<T>?): T {
//        SpecialPermission.check()
//        return try {
//            AccessController.doPrivileged(operation)
//        } catch (e: PrivilegedActionException) {
//            throw (e.cause as IOException?)!!
//        }
//    }
//
//    @JvmStatic
//    fun doPrivilegedVoid(action: Runnable) {
//        SpecialPermission.check()
//        AccessController.doPrivileged(
//            PrivilegedAction<Void?> {
//                action.run()
//                null
//            }
//        )
//    }
//}
