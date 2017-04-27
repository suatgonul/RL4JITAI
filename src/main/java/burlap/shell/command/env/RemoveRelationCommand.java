package burlap.shell.command.env;

import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentDelegation;
import burlap.oomdp.singleagent.environment.StateSettableEnvironment;
import burlap.shell.BurlapShell;
import burlap.shell.EnvironmentShell;
import burlap.shell.command.ShellCommand;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * A {@link burlap.shell.command.ShellCommand} for removing a relational target from an attribute for the current {@link burlap.oomdp.singleagent.environment.Environment}
 * {@link burlap.oomdp.core.states.State}. Use the -h option for help information.
 * @author James MacGlashan.
 */
public class RemoveRelationCommand implements ShellCommand {

	protected OptionParser parser = new OptionParser("avh*");



	@Override
	public String commandName() {
		return "removeRelation";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

		Environment env = ((EnvironmentShell)shell).getEnv();
		OptionSet oset = this.parser.parse(argString.split(" "));
		List<String> args = (List<String>)oset.nonOptionArguments();
		if(oset.has("h")){
			os.println("[-v][-a] objectName attribute [value]* \nRemoves a list of relations from an object's relational attribute in an " +
					"environment state, or removes all relational attributes from an attribute" +
					"First argument is the name of the object, then the attribute name and the list of relational values to remove." +
					"The environment must implement StateSettableEnvironment\n\n" +
					"-v print the new Environment state after completion.\n" +
					"-a removes all relational values from the attribute; the value argument list is not needed.");
			return 0;
		}

		StateSettableEnvironment senv = (StateSettableEnvironment) EnvironmentDelegation.EnvDelegationTools.getDelegateImplementing(env, StateSettableEnvironment.class);
		if(senv == null){
			os.println("Cannot set object values for environment states, because the environment does not implement StateSettableEnvironment");
			return 0;
		}

		if(oset.has("a")){
			if(args.size() != 2){
				return -1;
			}
		}
		else if(args.size() < 3){
			return -1;
		}

		State s = env.getCurrentObservation();
		ObjectInstance o = s.getObject(args.get(0));
		if(o == null){
			os.println("Unknown object " + args.get(0));
			return 0;
		}
		if(oset.has("a")){
			try{
				o.clearRelationalTargets(args.get(1));
			}catch(Exception e){
				os.println("Could not clear all relational values for attribute " + args.get(1) + ". Aborting.");
				return 0;
			}

		}
		else{
			for(int i = 2; i < args.size(); i++){
				try{
					o.removeRelationalTarget(args.get(1), args.get(i));
				}catch(Exception e){
					os.println("Could not add relational value " + args.get(2) + " to attribute " + args.get(i) + ". Aborting.");
					return 0;
				}
			}
		}

		senv.setCurStateTo(s);
		if(oset.has("v")){
			os.println(senv.getCurrentObservation().getCompleteStateDescriptionWithUnsetAttributesAsNull());
		}

		return 1;
	}
}
