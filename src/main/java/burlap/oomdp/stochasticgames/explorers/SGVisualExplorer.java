package burlap.oomdp.stochasticgames.explorers;

import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.explorer.TextAreaStreams;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.World;
import burlap.oomdp.stochasticgames.WorldObserver;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;
import burlap.oomdp.stochasticgames.agentactions.SGAgentAction;
import burlap.oomdp.stochasticgames.common.NullJointReward;
import burlap.oomdp.visualizer.Visualizer;
import burlap.shell.BurlapShell;
import burlap.shell.SGWorldShell;
import burlap.shell.ShellObserver;
import burlap.shell.command.world.JointActionCommand;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class allows you act as all of the agents in a stochastic game (controlled by a {@link burlap.oomdp.stochasticgames.World} object)
 * by choosing actions for each of them to take in specific states. A game with registered agents in the world can also
 * be played out (with some or all of the agents being manually controlled). States are
 * conveyed to the user through a 2D visualization and the user specifies actions for each agent
 * by pressing keys that are mapped to actions or by typing the actions into the action command field. After each
 * action is specified, the corresponding joint action is taken by pressing a special finalizing key that by default is set to "c".
 * The ` key
 * causes the state to reset to the initial state provided to the explorer or the {@link burlap.oomdp.stochasticgames.World}'s
 * {@link burlap.oomdp.stochasticgames.SGStateGenerator}. This explorer also associates itself with a {@link burlap.shell.SGWorldShell} so that additional commands can be given.
 * Keys can also be mapped to execute specific shell commands. You can access the shell with the
 * <p>
 * @author James MacGlashan
 *
 */
public class SGVisualExplorer extends JFrame implements ShellObserver, WorldObserver{

	private static final long serialVersionUID = 1L;
	
	
	protected SGDomain								domain;
	protected World									w;
	protected Map <String, GroundedSGAgentAction>		keyActionMap;
	protected Map <String, String>						keyShellMap;


	protected Visualizer 										painter;
	protected TextArea											propViewer;
	protected int												cWidth;
	protected int												cHeight;




	protected JFrame								consoleFrame;
	protected JTextArea								stateConsole;


	protected SGWorldShell 		shell;
	protected TextAreaStreams tstreams;



	/**
	 * Initializes the data members for the visual explorer.
	 * @param domain the stochastic game domain to be explored
	 * @param painter the 2D visualizer for states
	 * @param baseState the initial state from which to explore
	 */
	public SGVisualExplorer(SGDomain domain, Visualizer painter, State baseState){

		this(domain, painter, baseState, 800, 800);
	}

	/**
	 * Initializes the data members for the visual explorer.
	 * @param domain the stochastic game domain to be explored
	 * @param painter the 2D visualizer for states
	 * @param baseState the initial state from which to explore
	 * @param w the width of the state visualizer
	 * @param h the height of the state visualizer
	 */
	public SGVisualExplorer(SGDomain domain, Visualizer painter, State baseState, int w, int h){
		this.init(domain, new World(domain, new NullJointReward(), new NullTermination(), baseState), painter, w, h);
	}

	/**
	 * Initializes the data members for the visual explorer.
	 * @param domain the stochastic game domain to be explored
	 * @param world the world with which to interact
	 * @param painter the 2D visualizer for states
	 * @param w the width of the state visualizer
	 * @param h the height of the state visualizer
	 */
	public SGVisualExplorer(SGDomain domain, World world, Visualizer painter, int w, int h){
		this.init(domain, world, painter, w, h);
	}


	/**
	 * Initializes.
	 * @param domain the stochastic game domain
	 * @param world the {@link burlap.oomdp.stochasticgames.World} with which to interact
	 * @param painter the state {@link burlap.oomdp.visualizer.Visualizer}
	 * @param w the width of the state visualizer
	 * @param h the height of the state visualizer
	 */
	protected void init(SGDomain domain, World world, Visualizer painter, int w, int h){
		
		this.domain = domain;
		this.w = world;
		this.painter = painter;
		this.keyActionMap = new HashMap <String, GroundedSGAgentAction>();
		this.keyShellMap = new HashMap <String, String>();

		this.keyShellMap.put("`", "sg");
		
		this.cWidth = w;
		this.cHeight = h;
		
		this.propViewer = new TextArea();
		this.propViewer.setEditable(false);

		this.keyShellMap.put("c", "ja -x");

		this.w.addWorldObserver(this);

		
	}



	/**
	 * Specifies the action to set for a given key press. Actions should be formatted to include
	 * the agent name as follows: "agentName::actionName" This means
	 * that different key presses will have to specified for different agents.
	 * @param key the key that will cause the action to be set
	 * @param actionStringRep the action to set when the specified key is pressed.
	 */
	public void addKeyAction(String key, String actionStringRep){
		GroundedSGAgentAction action = this.parseIntoSingleActions(actionStringRep);
		if(action != null){
			keyActionMap.put(key, action);
		}
		else{
			System.out.println("Could not parse action string representation " + actionStringRep + ". SGVisualExplorer will not add a mapping to it from key " + key);
		}

	}

	
	/**
	 * Specifies the action to set for a given key press. Actions should be formatted to include
	 * the agent name as follows: "agentName::actionName" This means
	 * that different key presses will have to specified for different agents.
	 * @param key the key that will cause the action to be set
	 * @param action the action to set when the specified key is pressed.
	 */
	public void addKeyAction(String key, GroundedSGAgentAction action){
		keyActionMap.put(key, action);
	}


	/**
	 * Returns the {@link burlap.shell.SGWorldShell} associated with this visual explorer.
	 * @return the {@link burlap.shell.SGWorldShell} associated with this visual explorer.
	 */
	public SGWorldShell getShell() {
		return shell;
	}

	/**
	 * Returns the {@link burlap.oomdp.stochasticgames.World} associated with this explorer.
	 * @return the {@link burlap.oomdp.stochasticgames.World} associated with this explorer.
	 */
	public World getW() {
		return w;
	}

	/**
	 * Sets the {@link burlap.oomdp.stochasticgames.World} associated with this visual explorer and shell.
	 * @param w the {@link burlap.oomdp.stochasticgames.World} associated with this visual explorer and shell.
	 */
	public void setW(World w) {
		this.w = w;
		this.shell.setWorld(w);
	}

	/**
	 * Causes a shell command to be executed when a key is pressed with the visualizer in focus.
	 * @param key the key
	 * @param shellCommand the shell command to execute.
	 */
	public void addKeyShellCommand(String key, String shellCommand){
		this.keyShellMap.put(key, shellCommand);
	}

	/**
	 * Initializes the GUI and presents it to the user.
	 */
	public void initGUI(){
		
		painter.setPreferredSize(new Dimension(cWidth, cHeight));
		propViewer.setPreferredSize(new Dimension(cWidth, 100));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Container bottomContainer = new Container();
		bottomContainer.setLayout(new BorderLayout());
		bottomContainer.add(propViewer, BorderLayout.NORTH);

		getContentPane().add(bottomContainer, BorderLayout.SOUTH);
		getContentPane().add(painter, BorderLayout.CENTER);
	
		
		addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {	
			}
			public void keyTyped(KeyEvent e) {
				handleKeyPressed(e);
			}

		});
		
		//also add key listener to the painter in case the focus is changed
		painter.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {	
			}
			public void keyTyped(KeyEvent e) {
				handleKeyPressed(e);
			}

		});
		
		propViewer.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {	
			}
			public void keyTyped(KeyEvent e) {
				handleKeyPressed(e);
			}

		});
		





		JButton showConsoleButton = new JButton("Show Shell");
		showConsoleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SGVisualExplorer.this.consoleFrame.setVisible(true);
			}
		});
		bottomContainer.add(showConsoleButton, BorderLayout.SOUTH);


		this.consoleFrame = new JFrame();
		this.consoleFrame.setPreferredSize(new Dimension(600, 500));



		this.stateConsole = new JTextArea(40, 40);
		this.stateConsole.setLineWrap(true);
		DefaultCaret caret = (DefaultCaret)this.stateConsole.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		this.stateConsole.setEditable(false);
		this.stateConsole.setMargin(new Insets(10, 5, 10, 5));


		JScrollPane shellScroll = new JScrollPane(this.stateConsole, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		this.consoleFrame.getContentPane().add(shellScroll, BorderLayout.CENTER);

		this.tstreams = new TextAreaStreams(this.stateConsole);
		this.shell = new SGWorldShell(this.domain, tstreams.getTin(), new PrintStream(tstreams.getTout()), this.w);
		this.shell.addObservers(this);
		this.shell.setVisualizer(this.painter);
		this.shell.start();


		final JTextField consoleCommand = new JTextField(40);
		consoleCommand.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String command = ((JTextField)e.getSource()).getText();
				consoleCommand.setText("");
				tstreams.receiveInput(command + "\n");
			}
		});

		this.consoleFrame.getContentPane().add(consoleCommand, BorderLayout.SOUTH);


		pack();
		setVisible(true);

		this.consoleFrame.pack();
		this.consoleFrame.setVisible(false);

		this.updateState(this.w.getCurrentWorldState());
		
	}


	@Override
	public void observeCommand(BurlapShell shell, ShellCommandEvent event) {
		if(event.returnCode == 1){
			this.updateState(this.w.getCurrentWorldState());
		}
	}

	@Override
	public void gameStarting(State s) {
		this.updateState(s);
	}

	@Override
	public void observe(State s, JointAction ja, Map<String, Double> reward, State sp) {
		this.updateState(sp);
	}

	@Override
	public void gameEnding(State s) {
		//nothing
	}

	/**
	 * Updates the currently visualized state to the input state.
	 * @param s the state to visualize.
	 */
	public void updateState(State s){
		this.painter.updateState(s);
		this.updatePropTextArea(s);

	}


	
	private void handleKeyPressed(KeyEvent e){
		
		String key = String.valueOf(e.getKeyChar());
		

		//otherwise this could be an action, see if there is an action mapping
		GroundedSGAgentAction toAdd = keyActionMap.get(key);
		if(toAdd != null) {
			((JointActionCommand)this.shell.resolveCommand("ja")).addGroundedActionToJoint(toAdd);
		}

		else{

			String command = this.keyShellMap.get(key);
			if(command != null){
				this.shell.executeCommand(command);
			}

			
		}
		
		
	}




	
	protected void updatePropTextArea(State s){
		
		StringBuffer buf = new StringBuffer();
		
		List <PropositionalFunction> props = domain.getPropFunctions();
		for(PropositionalFunction pf : props){
			//List<GroundedProp> gps = s.getAllGroundedPropsFor(pf);
			List<GroundedProp> gps = pf.getAllGroundedPropsForState(s);
			for(GroundedProp gp : gps){
				if(gp.isTrue(s)){
					buf.append(gp.toString()).append("\n");
				}
			}
		}
		

		propViewer.setText(buf.toString());
		
		
	}

	/**
	 * Parses a string into a {@link burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction}. Expects format:
	 * "agentName:actionName param1 parm2 ... paramn" If there is no SingleAction by that name or
	 * the action and parameters are not applicable in the current state, null is returned.
	 * @param str string rep of a grounding action in the form  "agentName:actionName param1 parm2 ... paramn"
	 * @return a {@link burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction}
	 */
	protected GroundedSGAgentAction parseIntoSingleActions(String str){

		String [] agentActionComps = str.split(":");
		String aname = agentActionComps[0];

		String [] actionAndParams = agentActionComps[1].split(" ");
		String singleActionName = actionAndParams[0];

		String [] params = new String[actionAndParams.length-1];
		for(int i = 1; i < actionAndParams.length; i++){
			params[i-1] = actionAndParams[i];
		}

		SGAgentAction sa = domain.getSingleAction(singleActionName);
		if(sa == null){
			return null;
		}
		GroundedSGAgentAction gsa = sa.getAssociatedGroundedAction(aname);
		gsa.initParamsWithStringRep(params);
		if(!sa.applicableInState(this.w.getCurrentWorldState(), gsa)){
			return null;
		}

		return gsa;
	}


}
