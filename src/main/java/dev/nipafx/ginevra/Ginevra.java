package dev.nipafx.ginevra;

import dev.nipafx.args.Args;
import dev.nipafx.args.ArgsParseException;
import dev.nipafx.ginevra.config.GinevraArgs.ActionArgs;
import dev.nipafx.ginevra.config.GinevraArgs.BuildArgs;
import dev.nipafx.ginevra.config.GinevraArgs.DevelopArgs;
import dev.nipafx.ginevra.config.SiteConfiguration;
import dev.nipafx.ginevra.execution.Executor;

public class Ginevra {

	private Ginevra() {
		// private constructor to prevent initialization
	}

	public static void build(Class<? extends SiteConfiguration> configType, String[] args) {
		try {
			switch (Args.parseLeniently(args, ActionArgs.class)) {
				case BuildArgs buildArgs -> Executor.buildSite(configType, buildArgs, args);
				case DevelopArgs developArgs -> Executor.developSite(configType, developArgs, args);
			}
		} catch (ArgsParseException ex) {
			// TODO: handle error
			throw new IllegalArgumentException(ex);
		}
	}

}
