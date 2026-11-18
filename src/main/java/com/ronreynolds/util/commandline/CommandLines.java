package com.ronreynolds.util.commandline;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * YACLU - yet another command-line utility
 * tried all cmd-line processors i could find (and created several in the past) - this one seems the best of all of them
 * - simple, flexible, fast.
 * <p>
 * USAGE:
 * <code>
 * public static void main(String[] args) {
 * // create a map of key-value pairs from the command-line args
 * var argMap = CommandLines.argsToMap(args);
 * // retrieve the value of a key, or use a default if the key isn't found
 * String value = CommandLines.getOrDefault(argMap, "key", "defaultValue");
 * // OR use the thread-local set by argsToMap() (which makes it available on all methds called from this thread)
 * String value = CommandLines.getOrDefault("key", "defaultValue");
 * int intValue = CommandLines.getOrDefault("intKey", 42);
 * boolean boolValue = CommandLines.getOrDefault("boolKey", false);
 * double doubleValue = CommandLines.getOrDefault("doubleKey", 3.14);
 * ...
 * }
 * </code>
 */
public class CommandLines {
    private static final Pattern                               dashPrefix   = Pattern.compile("\\-+(.*)");
    private static final ThreadLocal<Multimap<String, String>> threadArgMap = new ThreadLocal<>();

    /**
     * convenience method for extracting the value from a --key=value formatted arg
     *
     * @param arg    the whole "--key=value" argument
     * @param prefix the "--key=" part for which we're testing the arg
     * @return an Optional around the value if the arg starts with the prefix; otherwise Optional.empty
     */
    public static Optional<String> getArgValue(String arg, String prefix) {
        return arg.startsWith(prefix) ? Optional.of(arg.substring(prefix.length())) : Optional.empty();
    }

    /**
     * this is the most general case that creates a new {@code Multimap<String,String>} and populates it with key-value pairs
     * as described below (Multimap so that a single arg-key can have multiple values)
     *
     * @param args the args passed into main()
     * @return a map populated with key-value pairs extracted from the args array
     */
    @SuppressWarnings("UnusedReturnValue")
    public static Multimap<String, String> argsToMap(String[] args) {
        Multimap<String, String> argMap = HashMultimap.create();
        // if there are no args, just set an empty map in the thread-local and return it
        if (args == null || args.length == 0) {
            threadArgMap.set(argMap);
            return argMap;
        }
        return argsToMap(args, argMap);
    }

    public static Multimap<String, String> argsToMap(String[] args, Consumer<String> unrecognizedArgs, String... recognizedArgs) {
        return argsToMap(args, HashMultimap.create(), unrecognizedArgs, recognizedArgs);
    }

    public static Multimap<String, String> argsToMap(String[] args, Multimap<String, String> results,
                                                     Consumer<String> unrecognizedArgs, String... recognizedArgs) {
        HashSet<String> recognizedArgSet = Sets.newHashSet(recognizedArgs);
        for (String arg : args) {
            Entry<String, String> entry = scrubAndSplitArg(arg);
            if (recognizedArgSet.contains(entry.getKey())) {
                results.put(entry.getKey(), entry.getValue());
            } else {
                unrecognizedArgs.accept(arg);
            }
        }
        threadArgMap.set(results);
        return results;
    }

    /**
     * this is a general version that string leading -'s from keys, splits key-value pairs on =, and populates a
     * provided Map with the results
     *
     * @param args    the args passed into main()
     * @param results allows the caller to provide an implementation of {@code Map<String,String>} which may or may not store
     *                anything at all (caller's discretion)
     * @return the results map passed in (for consistency with the above version)
     */
    public static Multimap<String, String> argsToMap(String[] args, Multimap<String, String> results) {
        for (String arg : args) {
            Entry<String, String> entry = scrubAndSplitArg(arg);
            results.put(entry.getKey(), entry.getValue());
        }
        threadArgMap.set(results);
        return results;
    }

    private static Entry<String, String> scrubAndSplitArg(String arg) {
        // trim any leading - or --
        Matcher mat = dashPrefix.matcher(arg);
        if (mat.matches()) {
            arg = mat.group(1);
        }
        int equals = arg.indexOf('=');
        return (equals > 0) ?
                new SimpleImmutableEntry<>(arg.substring(0, equals), arg.substring(Math.min(equals + 1, arg.length()))) :
                new SimpleImmutableEntry<>(arg, null);
    }

    public static boolean hasArgs() {
        return getArgCount() > 0;
    }

    public static int getArgCount() {
        var argMap = threadArgMap.get();
        return argMap != null ? argMap.size() : 0;
    }

    //
    // versions that simply take the key and default value
    //
    public static String getOrDefault(String key, String def) {
        return getOrDefault(key, Function.identity(), () -> def);
    }

    public static int getOrDefault(String key, int def) {
        return getOrDefault(key, Integer::parseInt, () -> def);
    }

    public static long getOrDefault(String key, long def) {
        return getOrDefault(key, Long::parseLong, () -> def);
    }

    public static double getOrDefault(String key, double def) {
        return getOrDefault(key, Double::parseDouble, () -> def);
    }

    public static <T extends Enum<T>> T getOrDefault(String key, Class<T> enumType, T def) {
        try {
            return Enum.valueOf(enumType, getOrDefault(key, null));
        } catch (NullPointerException | IllegalArgumentException iax) {
            return def;
        }
    }

    public static boolean getOrDefault(String key, boolean def) {
        Multimap<String, String> args = threadArgMap.get();
        Preconditions.checkState(args != null,
                "no arg-map provided and none attached to thread - did you call argsToMap() first?");

        Collection<String> val = Objects.requireNonNull(args.get(key));
        // boolean args need no value (if they're included without value they're considered true)
        if (val.isEmpty() || val.iterator().next() == null) {
            return args.containsKey(key) || def;
        }
        // if the map contains a value parse it as a boolean
        String value = val.iterator().next().toLowerCase();
        switch (value) {
            case "f":
            case "false":
            case "0":
                return false;
            default:
                return true;
        }
    }

    public static <T> T getOrDefault(String key, Function<String, ? extends T> converter, Supplier<? extends T> def) {
        return getOrDefault(threadArgMap.get(), key, converter, def);
    }

    // versions that take the arg-map
    public static String getOrDefault(Multimap<String, String> args, String key, String def) {
        return getOrDefault(args, key, Function.identity(), () -> def);
    }

    public static int getOrDefault(Multimap<String, String> args, String key, int def) {
        return getOrDefault(args, key, Integer::parseInt, () -> def);
    }

    public static long getOrDefault(Multimap<String, String> args, String key, long def) {
        return getOrDefault(args, key, Long::parseLong, () -> def);
    }

    public static double getOrDefault(Multimap<String, String> args, String key, double def) {
        return getOrDefault(args, key, Double::parseDouble, () -> def);
    }

    public static boolean getOrDefault(Multimap<String, String> args, String key, boolean def) {
        Preconditions.checkState(args != null, "no arg-map provided and none attached to thread - did you call argsToMap() " +
                "first?");

        Collection<String> val = Objects.requireNonNull(args.get(key));
        // boolean args need no value (if they're included without value they're considered true)
        if (val.isEmpty() || val.iterator().next() == null) {
            return args.containsKey(key) || def;
        }
        // if the map contains a value parse it as a boolean
        String value = val.iterator().next().toLowerCase();
        switch (value) {
            case "f":
            case "false":
            case "0":
                return false;
            default:
                return true;
        }
    }

    /**
     * handles everything except booleans that are considered true simply because they exist (tho it will handle bools
     * with values of true|false)
     *
     * @param args      map of argument key-value pairs
     * @param key       the key by which the arg is looked up
     * @param converter converter of the String value if the key is found
     * @param def       Supplier of the default value if the key isn't found
     * @param <T>       the type of the arg value to be returned (either found in the map or provided by 'def')
     * @return the value of the arg-key converted to the proper value or the default value if the key isn't found
     */
    public static <T> T getOrDefault(Multimap<String, String> args, String key, Function<String, ? extends T> converter,
                                     Supplier<? extends T> def) {
        if (args == null) {
            args = threadArgMap.get();
            Preconditions.checkState(args != null, "no arg-map provided and none attached to thread - did you call argsToMap() " +
                    "first?");
        }
        Collection<String> values = Objects.requireNonNull(args.get(key));
        String             val    = values.isEmpty() ? null : values.iterator().next();
        return StringUtils.isNotBlank(val) ? converter.apply(val) : (def != null ? def.get() : null);
    }
}