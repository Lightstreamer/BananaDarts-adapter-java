/*
Copyright (c) Lightstreamer Srl

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.lightstreamer.adapters.dart;

public class Constants {

    public static final String USER_SUBSCRIPTION = "user_";
    public static final String ROOMCHATLIST_SUBSCRIPTION = "roomchatlist_";
    public static final String ROOMCHAT_SUBSCRIPTION = "roomchat_";
    
    public static final String SPLIT_CHAR_REG = "\\|";
    public static final String SPLIT_CHAR = "|";
    
    public static final String LOGGER_CAT = "LS_demos_Logger.Dart.adapters";
    public static final String CHAT_CAT = "LS_demos_Logger.Dart.chat";
    public static final String WORLD_CAT = "LS_demos_Logger.Dart.world";
    
    public static final String USER_ID = "USER_ID";
    
    public static final String NICK_MESSAGE = "nick|";
    public static final String STATUS_MESSAGE = "status|";
    public static final String VOID_STATUS_ID = "0";
    public static final String ENTER_ROOM = "enter|";
    public static final String EXIT_ROOM = "leave|";
    
    public static final String RELEASE_MESSAGE = "release|";
    public static final String MOVE_MESSAGE = "move|";
    public static final String CHAT_MESSAGE = "chat|";
    public static final String RESET_MESSAGE = "reset|";
    
    public static String getVal(String original, String type) {
        if(original.indexOf(type) == 0) {
            return original.substring(type.length());
        }
        return null;
    }
}
