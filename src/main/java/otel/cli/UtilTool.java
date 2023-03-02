package otel.cli;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UtilTool {
	public static Optional<Set<String>> handleLabels(String labels) {
		String pattern = ".*-l(.*)";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(labels);
		if (m.find()) {
			String finds = m.group(1).trim().replace("\\|", ",");
			Set<String> set = Stream.of(finds.trim().split("\\s*,\\s*")).collect(Collectors.toSet());
			return Optional.of(set);
		} else {
			return Optional.empty();
		}
	}
}
