//package com.example.demo.config;
//
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class SqlParser {
//    public static String getTableNameFromSql(String sql) {
//        // 支持更复杂的 SQL 语句，如带别名、子查询等
//        Pattern pattern = Pattern.compile("FROM\\s+(\\w+)(?:\\s+AS\\s+\\w+)?");
//        Matcher matcher = pattern.matcher(sql.toUpperCase());
//        if (matcher.find()) {
//            return matcher.group(1);
//        }
//        return null;
//    }
//}