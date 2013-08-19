package org.gradle.api.plugins.clirr;

import net.sf.clirr.core.ApiDifference;
import net.sf.clirr.core.DiffListenerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BufferedListener extends DiffListenerAdapter {

    public static final Pattern METHOD_PATTERN = Pattern.compile(
            "((public|private|protected|static|final|native|synchronized|abstract)\\s)*(.+\\s)?([\\$_\\w]+)\\((.*)\\)"
    );

    private final Map<String, List<ApiDifference>> differences;
    private final List<Integer> ignoredDifferenceTypes;
    private final List<String> ignoredPackages;
    private final List<String> ignoredClasses;
    private final Map<String, List<String>> ignoredMembers;

    public BufferedListener(final List<Integer> ignoredDifferenceTypes,
                            final List<String> ignoredPackages,
                            final List<String> ignoredClasses,
                            final Map<String, List<String>> ignoredMembers) {
        this.ignoredDifferenceTypes = ignoredDifferenceTypes;
        this.ignoredPackages = ignoredPackages;
        this.ignoredClasses = ignoredClasses;
        this.ignoredMembers = ignoredMembers;
        this.differences = new HashMap<String, List<ApiDifference>>();
    }


    @Override
    public void reportDiff(final ApiDifference difference) {
        if (ignoredDifferenceTypes.contains(difference.getMessage().getId())) {
            return;
        }

        final String className = difference.getAffectedClass();
        final String packageName = className.substring(0, className.lastIndexOf('.'));

        if (ignoredPackages.contains(packageName) || ignoredClasses.contains(className)) {
            return;
        }

        final String memberName = extractMemberName(difference);

        if (memberName != null
                && ignoredMembers.containsKey(className)
                && ignoredMembers.get(className).contains(memberName)) {
            return;
        }

        if (!differences.containsKey(className)) {
            differences.put(className, new ArrayList<ApiDifference>());
        }
        differences.get(className).add(difference);
    }

    private static String extractSimpleName(final String className) {
        return className.substring(className.lastIndexOf('.') + 1);
    }

    @SuppressWarnings("unchecked")
    private static String extractMemberName(final ApiDifference difference) {
        if (difference.getAffectedMethod() != null) {
            final StringBuilder sb = new StringBuilder();
            final Matcher matcher = METHOD_PATTERN.matcher(difference.getAffectedMethod());
            if (matcher.matches()) {
                final String stripped = matcher.group(5).replaceAll("\\s+", "");
                if (matcher.group(3) != null) {
                    sb.append(matcher.group(4));
                } else {
                    sb.append(extractSimpleName(difference.getAffectedClass()));
                }
                sb.append("(").append(stripped).append(")");
                return sb.toString();
            } else {
                return null;
            }
        } else if (difference.getAffectedField() != null) {
            return difference.getAffectedField();
        } else {
            return null;
        }
    }


    public Map<String, List<ApiDifference>> getDifferences() {
        return differences;
    }
}
