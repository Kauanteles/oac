package architecture;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import components.Memory;

public class TestArchitecture {
	
	//uncomment the anotation below to run the architecture showing components status
	@Test
	public void testShowComponentes() {

		//a complete test (for visual purposes only).
		//a single code as follows
//		ldi 2
//		store 40
//		ldi -4
//		point:
//		store 41  //mem[41]=-4 (then -3, -2, -1, 0)
//		read 40
//		add 40    //mem[40] + mem[40]
//		store 40  //result must be in 40
//		read 41
//		inc
//		jn point
//		end
		
		Architecture arch = new Architecture(true);
		arch.getMemory().getDataList()[0]=14;
		arch.getMemory().getDataList()[1]=0;
		arch.getMemory().getDataList()[2]=0;
		arch.getMemory().getDataList()[3]=12;
		arch.getMemory().getDataList()[4]=0;
		arch.getMemory().getDataList()[5]=127;
		arch.getMemory().getDataList()[6]=14;
		arch.getMemory().getDataList()[7]=1;
		arch.getMemory().getDataList()[8]=0;
		arch.getMemory().getDataList()[6]=14;
		arch.getMemory().getDataList()[7]=0;
		arch.getMemory().getDataList()[8]=1;
		arch.getMemory().getDataList()[9]=14;
		arch.getMemory().getDataList()[10]=1;
		arch.getMemory().getDataList()[11]=2;
		arch.getMemory().getDataList()[12]=14;
		arch.getMemory().getDataList()[13]=5;
		arch.getMemory().getDataList()[14]=3;
		arch.getMemory().getDataList()[15]=11;
		arch.getMemory().getDataList()[16]=127;
		arch.getMemory().getDataList()[17]=1;
		arch.getMemory().getDataList()[18]=12;
		arch.getMemory().getDataList()[19]=0;
		arch.getMemory().getDataList()[20]=127;
		arch.getMemory().getDataList()[21]=13;
		arch.getMemory().getDataList()[22]=1;
		arch.getMemory().getDataList()[23]=0;
		arch.getMemory().getDataList()[24]=1;
		arch.getMemory().getDataList()[25]=127;
		arch.getMemory().getDataList()[26]=0;
		arch.getMemory().getDataList()[27]=4;
		arch.getMemory().getDataList()[28]=2;
		arch.getMemory().getDataList()[29]=3;
		arch.getMemory().getDataList()[30]=20;
		arch.getMemory().getDataList()[31]=2;
		arch.getMemory().getDataList()[32]=3;
		arch.getMemory().getDataList()[33]=18;
		arch.getMemory().getDataList()[34]=-1;
		arch.getMemory().getDataList()[127]=0;
		//now the program and the variables are stored. we can run
		arch.controlUnitEexec();
		
	}
	
	@Test
	public void testJmp() {
		Architecture arch = new Architecture();
		//storing the number 10 in PC
		arch.getExtbus1().put(10);
		arch.getPC().store();

		//storing the number 25 in the memory, in the position just before that one adressed by PC
		arch.getExtbus1().put(11); //the position is 11, once PC points to 10
		arch.getMemory().store();
		arch.getExtbus1().put(25);
		arch.getMemory().store();
		
		
		//testing if PC stores the number 10
		arch.getPC().read();
		assertEquals(10, arch.getExtbus1().get());
		
		//now we can perform the jmp method. 
		//we will move the the number 25 (stored in the 31th position in the memory) 
		//into the PC
		arch.jmp();
		arch.getPC().read();
		//the internalbus2 must contains the number 25
		assertEquals(25, arch.getExtbus1().get());
	}


	@Test
	public void testJz() {
		Architecture arch = new Architecture();
		
		//storing the number 30 in PC
		arch.getExtbus1().put(30);
		arch.getPC().store();
		
		//storing the number 25 in the into the memory, in position 31, the position just after PC
		arch.getExtbus1().put(31);
		arch.getMemory().store();
		arch.getExtbus1().put(25);
		arch.getMemory().store();

		//now we can perform the jz method. 

		//CASE 1.
		//Bit ZERO is equals to 1
		arch.getFlags().setBit(0, 1);
		
		//So, we will move the the number 25 (stored in the 31th position in the memory) 
		//into the PC

		//testing if PC stores the number 30
		arch.getPC().read();
		assertEquals(30, arch.getExtbus1().get());		

		arch.jz();
		
		//PC must be storng the number 25
		arch.getPC().read();
		assertEquals(25, arch.getExtbus1().get());
		
		//CASE 2.
		//Bit ZERO is equals to 0
		arch.getFlags().setBit(0, 0);
		//PC must have the number 30 initially (in this time, by using the external bus)
		arch.getExtbus1().put(30);
		arch.getPC().store();
		//destroying the data in external bus
		arch.getExtbus1().put(0);

		//testing if PC stores the number 30
		arch.getPC().read();
		assertEquals(30, arch.getExtbus1().get());	
		
		//Note that the memory was not changed. So, in position 31 we have the number 25
		
		//Once the ZERO bit is 0, we WILL NOT move the number 25 (stored in the 31th position in the memory)
		//into the PC.
		//The original PC position was 30. The parameter is in position 31. So, now PC must be pointing to 32
		arch.jz();
		//PC contains the number 32
		arch.getPC().read();
		assertEquals(32, arch.getExtbus1().get());
	}
	
	@Test
	public void testJn() {
		Architecture arch = new Architecture();
		
		//storing the number 30 in PC
		arch.getExtbus1().put(30);
		arch.getPC().store();
		
		//storing the number 25 in the into the memory, in position 31, the position just after PC
		arch.getExtbus1().put(31);
		arch.getMemory().store();
		arch.getExtbus1().put(25);
		arch.getMemory().store();

		//now we can perform the jn method. 

		//CASE 1.
		//Bit NEGATIVE is equals to 1
		arch.getFlags().setBit(1, 1);
		
		//So, we will move the the number 25 (stored in the 31th position in the memory) 
		//into the PC

		//testing if PC stores the number 30
		arch.getPC().read();
		assertEquals(30, arch.getExtbus1().get());		

		arch.jn();
		
		//PC must be storng the number 25
		arch.getPC().read();
		assertEquals(25, arch.getExtbus1().get());
		
		//CASE 2.
		//Bit NEGATIVE is equals to 0
		arch.getFlags().setBit(1, 0);
		//PC must have the number 30 initially (in this time, by using the external bus)
		arch.getExtbus1().put(30);
		arch.getPC().store();
		//destroying the data in external bus
		arch.getExtbus1().put(0);

		//testing if PC stores the number 30
		arch.getPC().read();
		assertEquals(30, arch.getExtbus1().get());	
		
		arch.jn();
		//PC contains the number 32
		arch.getPC().read();
		assertEquals(32, arch.getExtbus1().get());
	}
	
	@Test
	public void testJeq(){

		Architecture arch = new Architecture();
		//storing the number 1 in the memory, in position 31
		arch.getMemory().getDataList()[31]=0;
		//storing the number 0 in the memory, in position 32
		arch.getMemory().getDataList()[32]=1;
		arch.getMemory().getDataList()[33]=15;
		//making PC points to position 30
		arch.getExtbus1().put(30);
		arch.getPC().store();
		
		
		//now setting the registers values
		arch.getExtbus1().put(45);
		arch.getRegistersList().get(0).store(); //RPG0 has 45
		arch.getExtbus1().put(45);
		arch.getRegistersList().get(1).store(); //RPG1 has 99

		arch.jeq();

		arch.getPC().read();
		
		assertEquals(15, arch.getExtbus1().get());
	}

	@Test
	public void testJneq(){

		
		Architecture arch = new Architecture();
		//storing the number 1 in the memory, in position 31
		arch.getMemory().getDataList()[31]=0;
		//storing the number 0 in the memory, in position 32
		arch.getMemory().getDataList()[32]=1;
		arch.getMemory().getDataList()[33]=15;
		//making PC points to position 30
		arch.getExtbus1().put(30);
		arch.getPC().store();
		
		
		//now setting the registers values
		arch.getExtbus1().put(45);
		arch.getRegistersList().get(0).store(); //RPG0 has 45
		arch.getExtbus1().put(99);
		arch.getRegistersList().get(1).store(); //RPG1 has 99

		arch.jneq();

		arch.getPC().read();
		
		assertEquals(15, arch.getExtbus1().get());

	}

	@Test
	public void testJgt(){

		Architecture arch = new Architecture();
		//storing the number 1 in the memory, in position 31
		arch.getMemory().getDataList()[31]=0;
		//storing the number 0 in the memory, in position 32
		arch.getMemory().getDataList()[32]=1;
		arch.getMemory().getDataList()[33]=15;
		//making PC points to position 30
		arch.getExtbus1().put(30);
		arch.getPC().store();
		
		
		//now setting the registers values
		arch.getExtbus1().put(99);
		arch.getRegistersList().get(0).store(); //RPG0 has 45
		arch.getExtbus1().put(45);
		arch.getRegistersList().get(1).store(); //RPG1 has 99

		arch.jgt();

		arch.getPC().read();
		
		assertEquals(15, arch.getExtbus1().get());

	}

	@Test
	public void testJlw(){

		Architecture arch = new Architecture();
		//storing the number 1 in the memory, in position 31
		arch.getMemory().getDataList()[31]=0;
		//storing the number 0 in the memory, in position 32
		arch.getMemory().getDataList()[32]=1;
		arch.getMemory().getDataList()[33]=15;
		//making PC points to position 30
		arch.getExtbus1().put(30);
		arch.getPC().store();
		
		
		//now setting the registers values
		arch.getExtbus1().put(45);
		arch.getRegistersList().get(0).store(); //RPG0 has 45
		arch.getExtbus1().put(99);
		arch.getRegistersList().get(1).store(); //RPG1 has 99

		arch.jlw();

		arch.getPC().read();
		
		assertEquals(15, arch.getExtbus1().get());

	}

	@Test
	public void testInc(){

		Architecture arch = new Architecture();
		//storing the number 10 in RPG0
		arch.getExtbus1().put(10);
		arch.getRPG().store();
		//testing if RPG0 stores the number 10
		arch.getRPG().read();
		assertEquals(10, arch.getExtbus1().get());

		//destroying data in externalbus 1
		arch.getExtbus1().put(0);
		
		//pc points to 50 (where we suppose the instruction is
		arch.getExtbus1().put(50);
		arch.getPC().store();

		//now we can perform the inc method. 
		arch.inc();
		arch.getRPG().read();
		//the externalbus1 must contains the number 11
		assertEquals(11, arch.getExtbus1().get());
		
		//PC must be pointing ONE position after its original value, because this command has no parameters!
		arch.getPC().read();
		assertEquals(52, arch.getExtbus1().get());

	}

	@Test
	public void testaddRegReg() {
		Architecture arch = new Architecture();

		//storing the number 1 in the memory, in position 31
		arch.getMemory().getDataList()[31]=0;
		//storing the number 0 in the memory, in position 32
		arch.getMemory().getDataList()[32]=1;
		//making PC points to position 30
		arch.getExtbus1().put(30);
		arch.getPC().store();
		
		
		//now setting the registers values
		arch.getExtbus1().put(45);
		arch.getRegistersList().get(0).store(); //RPG0 has 45
		arch.getExtbus1().put(99);
		arch.getRegistersList().get(1).store(); //RPG1 has 99
		
		//executing the command move REG1 REG0.
		arch.addRegReg();
		
		//testing if both REG1 and REG0 store the same value: 99
		arch.getRegistersList().get(0).read();
		assertEquals(45, arch.getExtbus1().get());
		arch.getRegistersList().get(1).read();
		assertEquals(144, arch.getExtbus1().get());
		
		//Testing if PC points to 3 positions after the original
		//PC was pointing to 30; now it must be pointing to 33
		arch.getPC().read();
		
		assertEquals(33, arch.getExtbus1().get());
	}
		
			@Test
	public void testaddRegMem(){

		Architecture arch = new Architecture();

		//storing the number 1 in the memory, in position 31
		arch.getMemory().getDataList()[31]=0;
		//storing the number 0 in the memory, in position 32
		arch.getMemory().getDataList()[32]=52;
		arch.getMemory().getDataList()[52]=99;
		//making PC points to position 30
		arch.getExtbus1().put(30);
		arch.getPC().store();
		
		
		//now setting the registers values
		arch.getExtbus1().put(45);
		arch.getRegistersList().get(0).store(); //RPG0 has 45
		
		//executing the command move REG1 REG0.
		arch.addRegMem();
		
		//testing if both REG1 and REG0 store the same value: 99
		arch.getRegistersList().get(0).read();
		assertEquals(45, arch.getExtbus1().get());
		
		assertEquals(144, arch.getMemory().getDataList()[52]);
		
		//Testing if PC points to 3 positions after the original
		//PC was pointing to 30; now it must be pointing to 33
		arch.getPC().read();assertEquals(33, arch.getExtbus1().get());

	}

			@Test
	public void testaddMemReg(){

		Architecture arch = new Architecture();

		//storing the number 1 in the memory, in position 31
		arch.getMemory().getDataList()[31]=52;
		arch.getMemory().getDataList()[52]=45;
		//storing the number 0 in the memory, in position 32
		arch.getMemory().getDataList()[32]=0;
		//making PC points to position 30
		arch.getExtbus1().put(30);
		arch.getPC().store();
		
		
		//now setting the registers values
		arch.getExtbus1().put(99);
		arch.getRegistersList().get(0).store(); //RPG0 has 45
		
		//executing the command move REG1 REG0.
		arch.addMemReg();
		
		//testing if both REG1 and REG0 store the same value: 99
		arch.getRegistersList().get(0).read();
		assertEquals(144, arch.getExtbus1().get());
		
		//Testing if PC points to 3 positions after the original
		//PC was pointing to 30; now it must be pointing to 33
		arch.getPC().read();assertEquals(33, arch.getExtbus1().get());

	}

				@Test
	public void testaddImmReg(){

		Architecture arch = new Architecture();

		//storing the number 1 in the memory, in position 31
		arch.getMemory().getDataList()[31]=45;
		//storing the number 0 in the memory, in position 32
		arch.getMemory().getDataList()[32]=0;
		//making PC points to position 30
		arch.getExtbus1().put(30);
		arch.getPC().store();
		
		
		//now setting the registers values
		arch.getExtbus1().put(99);
		arch.getRegistersList().get(0).store(); //RPG0 has 45
		
		//executing the command move REG1 REG0.
		arch.addImmReg();
		
		//testing if both REG1 and REG0 store the same value: 99
		arch.getRegistersList().get(0).read();
		assertEquals(144, arch.getExtbus1().get());
		
		//Testing if PC points to 3 positions after the original
		//PC was pointing to 30; now it must be pointing to 33
		arch.getPC().read();assertEquals(33, arch.getExtbus1().get());

	}

	@Test
	public void testMoveImmReg(){

		Architecture arch = new Architecture();

		arch.getMemory().getDataList()[31]=2;
		
		arch.getMemory().getDataList()[32]=0;
		
		arch.getExtbus1().put(30);
		arch.getPC().store();
		
		
		
		arch.getExtbus1().put(45);
		arch.getRegistersList().get(0).store(); //RPG0 has 45

		arch.getRegistersList().get(0).read();
		assertEquals(45, arch.getExtbus1().get());
		
		arch.moveImmReg();

		arch.getRegistersList().get(0).read();
		assertEquals(2, arch.getExtbus1().get());

	}

	@Test
	public void testMoveRegReg(){

		Architecture arch = new Architecture();

		arch.getMemory().getDataList()[31]=0;
		
		arch.getMemory().getDataList()[32]=1;
		
		arch.getExtbus1().put(30);
		arch.getPC().store();
		
		
		
		arch.getExtbus1().put(45);
		arch.getRegistersList().get(0).store(); //RPG0 has 45
		arch.getExtbus1().put(52);
		arch.getRegistersList().get(1).store(); //RPG0 has 45
		
		arch.moveRegReg();

		arch.getRegistersList().get(1).read();
		assertEquals(45, arch.getExtbus1().get());

	}

	@Test
	public void testMoveMemReg(){

		Architecture arch = new Architecture();

		arch.getMemory().getDataList()[31]=45;
		arch.getMemory().getDataList()[45]=10;
		
		arch.getMemory().getDataList()[32]=1;
		
		arch.getExtbus1().put(30);
		arch.getPC().store();
		
		arch.getExtbus1().put(52);
		arch.getRegistersList().get(1).store(); //RPG0 has 45
		
		arch.moveMemReg();

		arch.getRegistersList().get(1).read();
		assertEquals(10, arch.getExtbus1().get());

	}
	
	@Test
	public void testMoveRegMem(){

		Architecture arch = new Architecture();

		arch.getMemory().getDataList()[31]=1;
		arch.getMemory().getDataList()[45]=10;
		
		arch.getMemory().getDataList()[32]=45;
		
		arch.getExtbus1().put(30);
		arch.getPC().store();
		
		arch.getExtbus1().put(52);
		arch.getRegistersList().get(1).store(); //RPG0 has 45
		
		arch.moveRegMem();

		arch.getExtbus1().put(45);
		arch.getMemory().read();
		assertEquals(52, arch.getExtbus1().get());

	}

	@Test
	public void testSubRegReg(){

		Architecture arch = new Architecture();

		//storing the number 1 in the memory, in position 31
		arch.getMemory().getDataList()[31]=0;
		//storing the number 0 in the memory, in position 32
		arch.getMemory().getDataList()[32]=1;
		//making PC points to position 30
		arch.getExtbus1().put(30);
		arch.getPC().store();
		
		
		//now setting the registers values
		arch.getExtbus1().put(45);
		arch.getRegistersList().get(0).store(); //RPG0 has 45
		arch.getExtbus1().put(99);
		arch.getRegistersList().get(1).store(); //RPG1 has 99
		
		//executing the command move REG1 REG0.
		arch.subRegReg();
		
		//testing if both REG1 and REG0 store the same value: 99
		arch.getRegistersList().get(0).read();
		assertEquals(45, arch.getExtbus1().get());
		arch.getRegistersList().get(1).read();
		assertEquals(-54, arch.getExtbus1().get());
		
		//Testing if PC points to 3 positions after the original
		//PC was pointing to 30; now it must be pointing to 33
		arch.getPC().read();assertEquals(33, arch.getExtbus1().get());

	}

		@Test
	public void testSubRegMem(){

		Architecture arch = new Architecture();

		//storing the number 1 in the memory, in position 31
		arch.getMemory().getDataList()[31]=0;
		//storing the number 0 in the memory, in position 32
		arch.getMemory().getDataList()[32]=52;
		arch.getMemory().getDataList()[52]=99;
		//making PC points to position 30
		arch.getExtbus1().put(30);
		arch.getPC().store();
		
		
		//now setting the registers values
		arch.getExtbus1().put(45);
		arch.getRegistersList().get(0).store(); //RPG0 has 45
		
		//executing the command move REG1 REG0.
		arch.subRegMem();
		
		//testing if both REG1 and REG0 store the same value: 99
		arch.getRegistersList().get(0).read();
		assertEquals(45, arch.getExtbus1().get());
		
		assertEquals(-54, arch.getMemory().getDataList()[52]);
		
		//Testing if PC points to 3 positions after the original
		//PC was pointing to 30; now it must be pointing to 33
		arch.getPC().read();assertEquals(33, arch.getExtbus1().get());

	}

			@Test
	public void testSubMemReg(){

		Architecture arch = new Architecture();

		//storing the number 1 in the memory, in position 31
		arch.getMemory().getDataList()[31]=52;
		arch.getMemory().getDataList()[52]=45;
		//storing the number 0 in the memory, in position 32
		arch.getMemory().getDataList()[32]=0;
		//making PC points to position 30
		arch.getExtbus1().put(30);
		arch.getPC().store();
		
		
		//now setting the registers values
		arch.getExtbus1().put(99);
		arch.getRegistersList().get(0).store(); //RPG0 has 45
		
		//executing the command move REG1 REG0.
		arch.subMemReg();
		
		//testing if both REG1 and REG0 store the same value: 99
		arch.getRegistersList().get(0).read();
		assertEquals(-54, arch.getExtbus1().get());
		
		//Testing if PC points to 3 positions after the original
		//PC was pointing to 30; now it must be pointing to 33
		arch.getPC().read();assertEquals(33, arch.getExtbus1().get());

	}

				@Test
	public void testSubImmReg(){

		Architecture arch = new Architecture();

		//storing the number 1 in the memory, in position 31
		arch.getMemory().getDataList()[31]=45;
		//storing the number 0 in the memory, in position 32
		arch.getMemory().getDataList()[32]=0;
		//making PC points to position 30
		arch.getExtbus1().put(30);
		arch.getPC().store();
		
		
		//now setting the registers values
		arch.getExtbus1().put(99);
		arch.getRegistersList().get(0).store(); //RPG0 has 45
		
		//executing the command move REG1 REG0.
		arch.subImmReg();
		
		//testing if both REG1 and REG0 store the same value: 99
		arch.getRegistersList().get(0).read();
		assertEquals(-54, arch.getExtbus1().get());
		
		//Testing if PC points to 3 positions after the original
		//PC was pointing to 30; now it must be pointing to 33
		arch.getPC().read();assertEquals(33, arch.getExtbus1().get());

	}

	

	@Test
	public void testFillCommandsList() {
		
		//all the instructions must be in Commands List
		/*
		 *
				add addr (rpg <- rpg + addr)
				sub addr (rpg <- rpg - addr)
				jmp addr (pc <- addr)
				jz addr  (se bitZero pc <- addr)
				jn addr  (se bitneg pc <- addr)
				read addr (rpg <- addr)
				store addr  (addr <- rpg)
				ldi x    (rpg <- x. x must be an integer)
				inc    (rpg++)
		 */

		
		
		Architecture arch = new Architecture();
		ArrayList<String> commands = arch.getCommandsList();
		assertTrue("add".equals(commands.get(0)));
		assertTrue("sub".equals(commands.get(1)));
		assertTrue("jmp".equals(commands.get(2)));
		assertTrue("jz".equals(commands.get(3)));
		assertTrue("jn".equals(commands.get(4)));
		assertTrue("read".equals(commands.get(5)));
		assertTrue("store".equals(commands.get(6)));
		assertTrue("ldi".equals(commands.get(7)));
		assertTrue("inc".equals(commands.get(8)));
	}
	
	@Test
	public void testReadExec() throws IOException {
		Architecture arch = new Architecture();
		arch.readExec("testFile");
		assertEquals(5, arch.getMemory().getDataList()[0]);
		assertEquals(4, arch.getMemory().getDataList()[1]);
		assertEquals(3, arch.getMemory().getDataList()[2]);
		assertEquals(2, arch.getMemory().getDataList()[3]);
		assertEquals(1, arch.getMemory().getDataList()[4]);
		assertEquals(0, arch.getMemory().getDataList()[5]);
	}

}
