package com.github.unixpackage.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ArgumentsTest {

	/**
	 * Ensure valid arguments are successfully processed in a Debian-based
	 * environment.
	 */
	@Test
	public void testArgumentsValidDebian() {
		// Define test for Debian-based OSs
		Variables.BUNDLE_MODE = "DEB";
		// Define String array with accepted arguments and valid values
		String[] arguments = "-b -c gpl3 -d Desc no. 1 -C i -D Desc no. 2 -s admin -e a.b@c.d -f a:b c:d -n A B -p some-name -V 0.1 -w http://u.rl"
				.split(" ");
		boolean argumentsCorrectlyParsed = Arguments
				.parseInputArguments(arguments);
		assertTrue(argumentsCorrectlyParsed);
	}

	/**
	 * Ensure valid arguments are successfully processed in a Red Hat-based
	 * environment.
	 */
	@Test
	public void testArgumentsValidRedHat() {
		// Define test for Debian-based OSs
		Variables.BUNDLE_MODE = "RPM";
		// Define String array with accepted arguments and valid values
		// "Group" not being tested as its validation fails...
		String[] arguments = "-b -c gpl3 -d Desc no. 1 -C i -D Desc no. 2 -e a.b@c.d -f a:b c:d -n A B -p some-name -V 0.1-1 -w http://u.rl"
				.split(" ");
		boolean argumentsCorrectlyParsed = Arguments
				.parseInputArguments(arguments);
		assertTrue(argumentsCorrectlyParsed);
	}

	/**
	 * Ensure invalid arguments are incorrectly processed.
	 */
	@Test
	public void testArgumentsNotAccepted() {
		String[] arguments = "a 3 -M".split(" ");
		boolean argumentsCorrectlyParsed = Arguments
				.parseInputArguments(arguments);
		assertFalse(argumentsCorrectlyParsed);
	}

}
