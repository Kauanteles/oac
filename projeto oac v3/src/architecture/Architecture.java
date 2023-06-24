package architecture;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


import components.Bus;
import components.Memory;
import components.Register;
import components.Ula;

public class Architecture {
	
	private boolean simulation; //this boolean indicates if the execution is done in simulation mode.
								//simulation mode shows the components' status after each instruction
	
	
	private boolean halt;
	private Bus extbus1;
	private Bus intbus1;
	private Bus intbus2;
	private Memory memory;
	private int memorySize;
	private Register PC;
	private Register IR;
	private Register RPG;
	private Register RPG1;
	private Register RPG2;
	private Register RPG3;
	private Register Flags;
	private Ula ula;
	private Bus demux; //only for multiple register purposes
	
	private ArrayList<String> commandsList;
	private ArrayList<Register> registersList;
	
	

	/**
	 * Instanciates all components in this architecture
	 */
	private void componentsInstances() {
		//don't forget the instantiation order
		//buses -> registers -> ula -> memory
		extbus1 = new Bus();
		intbus1 = new Bus();
		intbus2 = new Bus();
		PC = new Register("PC", extbus1, null);
		IR = new Register("IR", extbus1, intbus2);
		RPG = new Register("RPG0", extbus1, intbus1);
		RPG1 = new Register ("RPG1", extbus1, intbus1);
		RPG2 = new Register ("RPG2", extbus1, intbus1);
		RPG3 = new Register ("RPG3", extbus1, intbus1);
		Flags = new Register(2, intbus2);
		fillRegistersList();
		ula = new Ula(intbus1, intbus2);
		memorySize = 128;
		memory = new Memory(memorySize, extbus1);
		demux = new Bus(); //this bus is used only for multiple register operations
		
		fillCommandsList();
	}

	/**
	 * This method fills the registers list inserting into them all the registers we have.
	 * IMPORTANT!
	 * The first register to be inserted must be the default RPG
	 */
	private void fillRegistersList() {
		registersList = new ArrayList<Register>();
		registersList.add(RPG);
		registersList.add(RPG1);
		registersList.add(RPG2);
		registersList.add(RPG3);
		registersList.add(PC);
		registersList.add(IR);
		registersList.add(Flags);
	}

	/**
	 * Constructor that instanciates all components according the architecture diagram
	 */
	public Architecture() {
		componentsInstances();
		
		//by default, the execution method is never simulation mode
		simulation = false;
	}

	
	public Architecture(boolean sim) {
		componentsInstances();
		
		//in this constructor we can set the simoualtion mode on or off
		simulation = sim;
	}



	//getters
	
	protected Bus getExtbus1() {
		return extbus1;
	}

	protected Bus getIntbus1() {
		return intbus1;
	}

	protected Bus getIntbus2() {
		return intbus2;
	}

	protected Memory getMemory() {
		return memory;
	}

	protected Register getPC() {
		return PC;
	}

	protected Register getIR() {
		return IR;
	}

	protected Register getRPG() {
		return RPG;
	}

	protected Register getFlags() {
		return Flags;
	}

	protected Ula getUla() {
		return ula;
	}

	public ArrayList<String> getCommandsList() {
		return commandsList;
	}


	
	protected void fillCommandsList() {
		commandsList = new ArrayList<String>();
		commandsList.add("addRegReg"); //0
		commandsList.add("addMemReg"); //1
		commandsList.add("addRegMem"); //2

		commandsList.add("subRegReg"); //3
		commandsList.add("subMemReg"); //4
		commandsList.add("subRegMem"); //5

		commandsList.add("imulMemReg"); //6
		commandsList.add("imulRegMem");//7
		commandsList.add("imulRegReg"); //8

		commandsList.add("moveMemReg"); //9
		commandsList.add("moveRegMem"); //10
		commandsList.add("moveRegReg"); //11
		commandsList.add("moveImmReg"); //12

		commandsList.add("incReg"); //13
		commandsList.add("incMem"); //14

		commandsList.add("jmp"); //15
		commandsList.add("jn"); //16
		commandsList.add("jz"); //17
		commandsList.add("jnz"); //18

		commandsList.add("jeq"); //19
		commandsList.add("jgt"); //20
		commandsList.add("jlw"); //21
	}

	
	/**
	 * This method is used after some ULA operations, setting the flags bits according the result.
	 * @param result is the result of the operation
	 * NOT TESTED!!!!!!!
	 */
	private void setStatusFlags(int result) {
		Flags.setBit(0, 0);
		Flags.setBit(1, 0);
		if (result==0) { //bit 0 in flags must be 1 in this case
			Flags.setBit(0,1);
		}
		if (result<0) { //bit 1 in flags must be 1 in this case
			Flags.setBit(1,1);
		}
	}


	public void addRegReg(){ 
		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//RPG A -> ula(0)
		memory.read();
		demux.put(extbus1.get());
		registersInternalRead();
		ula.store(0);

		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//RPG B -> ula(1)
		memory.read();
		demux.put(extbus1.get());
		registersInternalRead();
		ula.store(1);
		
		//ulaAdd -> RPG B
		ula.add();
		ula.internalRead(1);
		setStatusFlags(intbus2.get());
		ula.read(1);
		registersInternalStore();

		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();
	}

	public void addMemReg(){
		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//mem -> ula(0)
		memory.read();
		memory.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(0);
		
		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//Reg A -> ula(1)
		memory.read();
		demux.put(extbus1.get());
		registersInternalRead();
		ula.store(1);

		//ulaAdd -> Reg A
		ula.add();
		ula.internalRead(1);
		setStatusFlags(intbus2.get());
		ula.read(1);
		registersInternalStore();

		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();
	}

	public void addRegMem(){
		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//Reg A -> ula(0)
		memory.read();
		demux.put(extbus1.get());
		registersInternalRead();
		ula.store(0);

		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//Mem -> ula(1)
		memory.read();
		memory.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);

		//ulaAdd -> Mem
		ula.add();
		ula.internalRead(1);
		setStatusFlags(intbus2.get());
		IR.internalStore();
		PC.read();
		memory.read();
		memory.store();
		IR.read();
		memory.store();

		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();
	}

	
	public void subRegReg() {
		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//RPG A -> ula(0)
		memory.read();
		demux.put(extbus1.get());

		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//RPGB -> ula(1)
		PC.read();
		memory.read();
		registersInternalRead();
		ula.store(1);
		demux.put(extbus1.get());
		registersInternalRead();
		ula.store(0);
		
		//ulaAdd -> RPGB
		ula.sub();
		ula.internalRead(1);
		setStatusFlags(intbus2.get());
		ula.read(1);
		registersInternalStore();

		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();
	}

	public void subMemReg(){
		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//Mem -> ula(0)
		memory.read();
		memory.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(0);

		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//Mem-Reg A -> Reg A
		memory.read();
		demux.put(extbus1.get());
		registersInternalRead();
		ula.internalRead(0);
		ula.store(1);
		ula.internalStore(0);
		ula.sub();
		ula.internalRead(1);
		setStatusFlags(intbus2.get());
		ula.read(1);
		registersInternalStore();

		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();
	}

	public void subRegMem(){
		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//Reg A -> intBus1
		memory.read();
		demux.put(extbus1.get());
		registersInternalRead();

		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//Reg A - Mem -> Mem
		memory.read();
		memory.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(0);
		ula.store(1);
		ula.sub();
		ula.internalRead(1);
		setStatusFlags(intbus2.get());
		IR.internalStore();
		PC.read();
		memory.read();
		memory.store();
		IR.read();
		memory.store();

		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();
	}

	public void moveMemReg(){
		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//Mem -> ula(0)
		memory.read();
		memory.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(0);

		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//ula(0) -> Reg A
		memory.read();
		demux.put(extbus1.get());
		ula.read(0);
		registersInternalStore();

		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();
	}

	public void moveRegMem(){
		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		memory.read();
		demux.put(extbus1.get());

		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//Reg A -> Mem
		memory.read();
		memory.store();
		registersRead();
		memory.store();

		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();
	}

	public void moveRegReg(){
		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//Reg A -> ula(0)
		memory.read();
		demux.put(extbus1.get());
		registersInternalRead();
		ula.store(0);

		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//ula(0) -> Reg B
		memory.read();
		demux.put(extbus1.get());
		ula.read(0);
		registersInternalStore();

		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();
	}

	public void incReg(){
		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//Reg A -> ula(1)
		memory.read();
		demux.put(extbus1.get());
		registersInternalRead();
		ula.store(1);

		//ula++ -> Reg A
		ula.inc();
		ula.read(1);
		registersInternalStore();

		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();
	}

	public void incMem(){
		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		memory.read();
		registersList.get(0).internalRead();
		ula.store(0);
		registersList.get(0).store();
		memory.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		registersList.get(0).read();
		memory.store();
		IR.read();
		memory.store();
		ula.read(0);
		registersList.get(0).internalStore();

		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();
	}

	public void jmp(){
		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//Mem -> PC
		memory.read();
		PC.store();
	}

	public void jn(){
		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		if (Flags.getBit(1)==1){
			PC.read();
			memory.read();
			PC.store();
		}
		else{
			//PC++
			PC.read();
			IR.store();
			IR.internalRead();
			ula.internalStore(1);
			ula.inc();
			ula.internalRead(1);
			IR.internalStore();
			IR.read();
			PC.store();
		}
	}

	public void jz(){
		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		if (Flags.getBit(0)==1){
			PC.read();
			memory.read();
			PC.store();
		}
		else{
			//PC++
			PC.read();
			IR.store();
			IR.internalRead();
			ula.internalStore(1);
			ula.inc();
			ula.internalRead(1);
			IR.internalStore();
			IR.read();
			PC.store();
		}
	}
	
	//possivelmente errado
	public void jnz(){
		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		if ((Flags.getBit(2)==1)){
			PC.read();
			memory.read();
			PC.store();
		}
		else{
			//PC++
			PC.read();
			IR.store();
			IR.internalRead();
			ula.internalStore(1);
			ula.inc();
			ula.internalRead(1);
			IR.internalStore();
			IR.read();
			PC.store();
		}
	}

	public void jeq(){
		//subtrai ambos os registradores e faz um jz, ou seja, A-B=0 = A=B
		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//RPG A -> ula(0)
		PC.read();
		memory.read();
		demux.put(extbus1.get());

		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//RPG B -> ula(1)
		PC.read();
		memory.read();
		registersInternalRead();
		ula.store(1);
		demux.put(extbus1.get());
		registersInternalRead();
		ula.store(0);
		
		//ulaAdd -> RPGB
		ula.sub();
		ula.internalRead(1);
		setStatusFlags(intbus2.get());

		jz();
	}

	public void jgt(){
		//subtrai ambos os registradores e faz um jz, ou seja, A-B=0 = A=B
		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//RPGA -> ula(0)
		PC.read();
		memory.read();
		demux.put(extbus1.get());

		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//RPGB -> ula(1)
		PC.read();
		memory.read();
		registersInternalRead();
		ula.store(1);
		demux.put(extbus1.get());
		registersInternalRead();
		ula.store(0);
		
		//ulaAdd -> RPGB
		ula.sub();
		ula.internalRead(1);
		setStatusFlags(intbus2.get());

		jnz();
	}

	public void jlw(){
		//subtrai ambos os registradores e faz um jz, ou seja, A-B=0 = A=B
		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//RPGA -> ula(0)
		PC.read();
		memory.read();
		demux.put(extbus1.get());

		//PC++
		PC.read();
		IR.store();
		IR.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		IR.internalStore();
		IR.read();
		PC.store();

		//RPGB -> ula(1)
		PC.read();
		memory.read();
		registersInternalRead();
		ula.store(1);
		demux.put(extbus1.get());
		registersInternalRead();
		ula.store(0);
		
		//ulaAdd -> RPGB
		ula.sub();
		ula.internalRead(1);
		setStatusFlags(intbus2.get());

		jn();
	}


	public void read() {
		PC.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.internalStore(); //now PC points to the parameter address
		PC.read(); 
		memory.read(); // the address is now in the external bus.
		memory.read(); // the data is now in the external bus.
		RPG.store();
		PC.internalRead(); //we need to make PC points to the next instruction address
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.internalStore(); //now PC points to the next instruction. We go back to the FETCH status.
	}
	
	/**
	 * This method implements the microprogram for
	 * 					store address
	 * In the machine language this command number is 6, and the address is in the position next to him
	 *    
	 * where address is a valid position in this memory architecture 
	 * The method reads the value from RPG (the first register in the register list) and 
	 * inserts it into the memory (position address) 
	 * The logic is
	 * 1. pc -> intbus2 //pc.read()
	 * 2. ula <-  intbus2 //ula.store()
	 * 3. ula incs
	 * 4. ula -> intbus2 //ula.read()
	 * 5. pc <- intbus2 //pc.store() now pc points to the parameter
	 * 6. pc -> extbus //(pc.read())the parameter address is the external bus
	 * 7. memory reads // memory reads the data in the parameter address. 
	 * 					// this data is the address where the RPG value must be stores 
	 * 8. memory stores //memory reads the address and wait for the value
	 * 9. RPG -> Externalbus //RPG.read()
	 * 10. memory stores //memory receives the value and stores it
	 * 11. pc -> intbus2 //pc.read() now pc must point the next instruction address
	 * 12. ula <- intbus2 //ula.store()
	 * 13. ula incs
	 * 14. ula -> intbus2 //ula.read()
	 * 15. pc <- intbus2 //pc.store() 
	 * end
	 * @param address
	 */
	public void store() {
		PC.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.internalStore(); //now PC points to the parameter address
		PC.read(); 
		memory.read();   //the parameter address (pointing to the addres where data must be stored
		                 //is now in externalbus1
		memory.store(); //the address is in the memory. Now we must to send the data
		RPG.read();
		memory.store(); //the data is now stored
		PC.internalRead(); //we need to make PC points to the next instruction address
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.internalStore(); //now PC points to the next instruction. We go back to the FETCH status.
	}
	
	/**
	 * This method implements the microprogram for
	 * 					ldi immediate
	 * In the machine language this command number is 7, and the immediate value
	 *        is in the position next to him
	 *    
	 * The method moves the value (parameter) into the internalbus1 and the RPG 
	 * (the first register in the register list) consumes it 
	 * The logic is
	 * 1. pc -> intbus2 //pc.read()
	 * 2. ula <-  intbus2 //ula.store()
	 * 3. ula incs
	 * 4. ula -> intbus2 //ula.read()
	 * 5. pc <- intbus2 //pc.store() now pc points to the parameter
	 * 6. pc -> extbus //(pc.read())the address where is the position to be read is now in the external bus 
	 * 7. memory reads from extbus //this forces memory to write the stored data in the extbus
	 * 8. RPG <- extbus //rpg.store()
	 * 9. 10. pc -> intbus2 //pc.read() now pc must point the next instruction address
	 * 10. ula <- intbus2 //ula.store()
	 * 11. ula incs
	 * 12. ula -> intbus2 //ula.read()
	 * 13. pc <- intbus2 //pc.store() 
	 * end
	 * @param address
	 */
	public void ldi() {
		PC.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.internalStore(); //now PC points to the parameter address
		PC.read(); 
		memory.read(); // the immediate is now in the external bus.
		RPG.store();   //RPG receives the immediate
		PC.internalRead(); //we need to make PC points to the next instruction address
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.internalStore(); //now PC points to the next instruction. We go back to the FETCH status.
	}
	
	/**
	 * This method implements the microprogram for
	 * 					inc 
	 * In the machine language this command number is 8
	 *    
	 * The method moves the value in rpg (the first register in the register list)
	 *  into the ula and performs an inc method
	 * 		-> inc works just like add rpg (the first register in the register list)
	 *         with the mumber 1 stored into the memory
	 * 		-> however, inc consumes lower amount of cycles  
	 * 
	 * The logic is
	 * 
	 * 1. rpg -> intbus1 //rpg.read()
	 * 2. ula  <- intbus1 //ula.store()
	 * 3. Flags <- zero //the status flags are reset
	 * 4. ula incs
	 * 5. ula -> intbus1 //ula.read()
	 * 6. ChangeFlags //informations about flags are set according the result
	 * 7. rpg <- intbus1 //rpg.store()
	 * 8. pc -> intbus2 //pc.read() now pc must point the next instruction address
	 * 9. ula <- intbus2 //ula.store()
	 * 10. ula incs
	 * 11. ula -> intbus2 //ula.read()
	 * 12. pc <- intbus2 //pc.store()
	 * end
	 * @param address
	 */
	public void inc() {
		RPG.internalRead();
		ula.store(1);
		ula.inc();
		ula.read(1);
		setStatusFlags(intbus1.get());
		RPG.internalStore();
		PC.internalRead(); //we need to make PC points to the next instruction address
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.internalStore(); //now PC points to the next instruction. We go back to the FETCH status.
	}
	
	/**
	 * This method implements the microprogram for
	 * 					move <reg1> <reg2> 
	 * In the machine language this command number is 9
	 *    
	 * The method reads the two register ids (<reg1> and <reg2>) from the memory, in positions just after the command, and
	 * copies the value from the <reg1> register to the <reg2> register
	 * 
	 * 1. pc -> intbus2 //pc.read()
	 * 2. ula <-  intbus2 //ula.store()
	 * 3. ula incs
	 * 4. ula -> intbus2 //ula.read()
	 * 5. pc <- intbus2 //pc.store() now pc points to the first parameter
	 * 6. pc -> extbus //(pc.read())the address where is the position to be read is now in the external bus 
	 * 7. memory reads from extbus //this forces memory to write the parameter (first regID) in the extbus
	 * 8. pc -> intbus2 //pc.read() //getting the second parameter
	 * 9. ula <-  intbus2 //ula.store()
	 * 10. ula incs
	 * 11. ula -> intbus2 //ula.read()
	 * 12. pc <- intbus2 //pc.store() now pc points to the second parameter
	 * 13. demux <- extbus //now the register to be operated is selected
	 * 14. registers -> intbus1 //this performs the internal reading of the selected register 
	 * 15. PC -> extbus (pc.read())the address where is the position to be read is now in the external bus 
	 * 16. memory reads from extbus //this forces memory to write the parameter (second regID) in the extbus
	 * 17. demux <- extbus //now the register to be operated is selected
	 * 18. registers <- intbus1 //thid rerforms the external reading of the register identified in the extbus
	 * 19. 10. pc -> intbus2 //pc.read() now pc must point the next instruction address
	 * 20. ula <- intbus2 //ula.store()
	 * 21. ula incs
	 * 22. ula -> intbus2 //ula.read()
	 * 23. pc <- intbus2 //pc.store()  
	 * 		  
	 */
	
	
	public ArrayList<Register> getRegistersList() {
		return registersList;
	}

	/**
	 * This method performs an (external) read from a register into the register list.
	 * The register id must be in the demux bus
	 */
	private void registersRead() {
		registersList.get(demux.get()).read();
	}
	
	/**
	 * This method performs an (internal) read from a register into the register list.
	 * The register id must be in the demux bus
	 */
	private void registersInternalRead() {
		registersList.get(demux.get()).internalRead();;
	}
	
	/**
	 * This method performs an (external) store toa register into the register list.
	 * The register id must be in the demux bus
	 */
	private void registersStore() {
		registersList.get(demux.get()).store();
	}
	
	/**
	 * This method performs an (internal) store toa register into the register list.
	 * The register id must be in the demux bus
	 */
	private void registersInternalStore() {
		registersList.get(demux.get()).internalStore();;
	}



	/**
	 * This method reads an entire file in machine code and
	 * stores it into the memory
	 * NOT TESTED
	 * @param filename
	 * @throws IOException 
	 */
	public void readExec(String filename) throws IOException {
		   BufferedReader br = new BufferedReader(new		 
		   FileReader(filename+".dxf"));
		   String linha;
		   int i=0;
		   while ((linha = br.readLine()) != null) {
			     extbus1.put(i);
			     memory.store();
			   	 extbus1.put(Integer.parseInt(linha));
			     memory.store();
			     i++;
			}
			br.close();
	}
	
	/**
	 * This method executes a program that is stored in the memory
	 */
	public void controlUnitEexec() {
		halt = false;
		while (!halt) {
			fetch();
			decodeExecute();
		}

	}
	

	/**
	 * This method implements The decode proccess,
	 * that is to find the correct operation do be executed
	 * according the command.
	 * And the execute proccess, that is the execution itself of the command
	 */
	private void decodeExecute() {
		IR.internalRead(); //the instruction is in the internalbus2
		int command = intbus2.get();
		simulationDecodeExecuteBefore(command);
		switch (command) {
		case 0:
			addRegReg();
			break;
		case 1:
			subRegReg();
			break;
		case 2:
			jmp();
			break;
		case 3:
			jz();
			break;
		case 4:
			jn();
			break;
		case 5:
			read();
			break;
		case 6:
			store();
			break;
		case 7:
			ldi();
			break;
		case 8:
			inc();
			break;
		default:
			halt = true;
			break;
		}
		if (simulation)
			simulationDecodeExecuteAfter();
	}

	/**
	 * This method is used to show the components status in simulation conditions
	 * NOT TESTED
	 * @param command 
	 */
	private void simulationDecodeExecuteBefore(int command) {
		System.out.println("----------BEFORE Decode and Execute phases--------------");
		String instruction;
		int parameter = 0;
		for (Register r:registersList) {
			System.out.println(r.getRegisterName()+": "+r.getData());
		}
		if (command !=-1)
			instruction = commandsList.get(command);
		else
			instruction = "END";
		if (hasOperands(instruction)) {
			parameter = memory.getDataList()[PC.getData()+1];
			System.out.println("Instruction: "+instruction+" "+parameter);
		}
		else
			System.out.println("Instruction: "+instruction);
		if ("read".equals(instruction))
			System.out.println("memory["+parameter+"]="+memory.getDataList()[parameter]);
		
	}

	/**
	 * This method is used to show the components status in simulation conditions
	 * NOT TESTED 
	 */
	private void simulationDecodeExecuteAfter() {
		String instruction;
		System.out.println("-----------AFTER Decode and Execute phases--------------");
		System.out.println("Internal Bus 1: "+intbus1.get());
		System.out.println("Internal Bus 2: "+intbus2.get());
		System.out.println("External Bus 1: "+extbus1.get());
		for (Register r:registersList) {
			System.out.println(r.getRegisterName()+": "+r.getData());
		}
		Scanner entrada = new Scanner(System.in);
		System.out.println("Press <Enter>");
		String mensagem = entrada.nextLine();
	}

	/**
	 * This method uses PC to find, in the memory,
	 * the command code that must be executed.
	 * This command must be stored in IR
	 * NOT TESTED!
	 */
	private void fetch() {
		PC.read();
		memory.read();
		IR.store();
		simulationFetch();
	}

	/**
	 * This method is used to show the components status in simulation conditions
	 * NOT TESTED!!!!!!!!!
	 */
	private void simulationFetch() {
		if (simulation) {
			System.out.println("-------Fetch Phase------");
			System.out.println("PC: "+PC.getData());
			System.out.println("IR: "+IR.getData());
		}
	}

	/**
	 * This method is used to show in a correct way the operands (if there is any) of instruction,
	 * when in simulation mode
	 * NOT TESTED!!!!!
	 * @param instruction 
	 * @return
	 */
	private boolean hasOperands(String instruction) {
		if ("inc".equals(instruction)) //inc is the only one instruction having no operands
			return false;
		else
			return true;
	}

	/**
	 * This method returns the amount of positions allowed in the memory
	 * of this architecture
	 * NOT TESTED!!!!!!!
	 * @return
	 */
	public int getMemorySize() {
		return memorySize;
	}
	
	public static void main(String[] args) throws IOException {
		Architecture arch = new Architecture(true);
		arch.readExec("program");
		arch.controlUnitEexec();

		
	}
	

}
