/* 
 * ****************************************************************************
 * RP6 ROBOT SYSTEM - ROBOT BASE EXAMPLES
 * ****************************************************************************
 * Example: Movement 5
 * Author(s): Dominik S. Herwald
 * ****************************************************************************
 * Description:
 *
 * In this example we add a new behaviour to our subsumption architechture. 
 * We call it "avoid". This behaviour tries to avoid collisions with 
 * obstacles by using the two ACS channels. 
 *
 * The escape behaviour from the previous example is NOT changed at all! 
 * Also the other routines stay like they are, except that we add the new
 * Behaviour to the behaviourController function.
 *
 * ############################################################################
 * #+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+
 * 
 * ATTENTION: THE ROBOT MOVES AROUND IN THIS EXAMPLE! PLEASE PROVIDE ABOUT
 * 2m x 2m OR MORE FREE SPACE FOR THE ROBOT! 
 *
 * >>> DO NOT FORGET TO REMOVE THE FLAT CABLE CONNECTION TO THE USB INTERFACE
 * BEFORE YOU START THIS PROGRAM BY PRESSING THE START BUTTON ON THE ROBOT!
 *
 * #+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+#+
 * ############################################################################
 * ****************************************************************************
 */

/*****************************************************************************/
// Includes:

#include "RP6RobotBaseLib.h" 	
#include <stdio.h>

/*****************************************************************************/
// Behaviour command type:

#define IDLE  0

// The behaviour command data type:
typedef struct {
	uint8_t  speed_left;  // left speed (is used for rotation and 
						  // move distance commands - if these commands are 
						  // active, speed_right is ignored!)
	uint8_t  speed_right; // right speed
	unsigned dir:2;       // direction (FWD, BWD, LEFT, RIGHT)
	unsigned move:1;      // move flag
	unsigned rotate:1;    // rotate flag
	uint16_t move_value;  // move value is used for distance and angle values
	uint8_t  state;       // state of the behaviour
} behaviour_command_t;


behaviour_command_t STOP = {0, 0, FWD, false, false, 0, IDLE};

/*****************************************************************************/
// Cruise Behaviour:

#define CRUISE_SPEED_FWD    500 //40 // 100 Default speed when no obstacles are detected!

#define MOVE_FORWARDS 1
behaviour_command_t cruise = {CRUISE_SPEED_FWD, CRUISE_SPEED_FWD, FWD, 
								false, false, 0, MOVE_FORWARDS};

const char *direction[4] = {"FWD", "BWD", "LEFT", "RIGHT"};

int lastState = 0;

void mywrite(const char *pstring, int state)
{
    if (state != lastState)
    {
        lastState = state;
        uint8_t c;
        for (;(c = *pstring++);writeChar(c));
    }
}

/**
 * We don't have anything to do here - this behaviour has only
 * a constant value for moving forwards - s. above!
 * Of course you can change this and add some random or timed movements 
 * in here...
 */
void behaviour_cruise(void)
{
    uint8_t charsToReceive = 16;
    
    //creat a new, clear buffer
    char receiveBuffer[charsToReceive+1];
    clearReceptionBuffer();
    uint8_t cnt;
    for(cnt = 0; cnt < charsToReceive; cnt++) {
        receiveBuffer[cnt]=0;
    }

    uint8_t buffer_pos = 0;
//    while(true)
//    {
        while(getBufferLength())
        {
            receiveBuffer[buffer_pos] = readChar();
            if(receiveBuffer[buffer_pos] == '\n')
            {
                receiveBuffer[buffer_pos] = '\0';
                buffer_pos = 0;
                break;
            }
            else if(buffer_pos >= charsToReceive)
            {
                receiveBuffer[charsToReceive] = '\0';
                writeString_P("\n\nYou entered more characters than possible!\n");
                break;
            }
            buffer_pos++;
        }
//    }

    writeChar('\n');

//    writeString_P("Your input was (Bytes):\n");
//
//    for(cnt = 0; cnt < charsToReceive; cnt++) {
//        writeInteger(receiveBuffer[cnt],DEC);
//        writeChar(',');
//    }
//    writeInteger(receiveBuffer[charsToReceive],DEC);
//    writeString_P("\n");

    writeString_P("received: ");
    writeString(receiveBuffer);
    writeString_P("\n");


//    uint8_t length = getBufferLength();
//    if (length > 0){
////         readChars(char *buf, uint8_t numberOfChars);
//        char buf[length+1];
//        readChars(buf, length);
//        char text[length+55];
//        sprintf(text, "data read: %s; length: %d", buf, length);
//        mywrite(text, 42);
//        if (buf[0] == 's') {
//			STOP.dir = FWD;
//            STOP.rotate = 0;
//            STOP.speed_left = 0;
//            STOP.speed_right = 0;
//			STOP.move = false;
//            moveCommand(&STOP);
//            writeString_P("STOP\n");
//        } else {
//            writeString_P("\n->fu\n");
//        }
//    }
}

/*****************************************************************************/
// Escape Behaviour:

#define ESCAPE_SPEED_BWD    100 //40 // 100
#define ESCAPE_SPEED_ROTATE 100 //30  // 60

#define ESCAPE_FRONT		1
#define ESCAPE_FRONT_WAIT 	2
#define ESCAPE_LEFT  		3
#define ESCAPE_LEFT_WAIT	4
#define ESCAPE_RIGHT	    5
#define ESCAPE_RIGHT_WAIT 	6
#define ESCAPE_WAIT_END		7
behaviour_command_t escape = {0, 0, FWD, false, false, 0, IDLE}; 

/**
 * This is the Escape behaviour for the Bumpers.
 */

void behaviour_escape(void)
{
	static uint8_t bump_count = 0;
	
	switch(escape.state)
	{
		case IDLE: 
		break;
		case ESCAPE_FRONT:
			escape.speed_left = ESCAPE_SPEED_BWD;
			escape.dir = BWD;
			escape.move = true;
			if(bump_count > 3)
				escape.move_value = 220;
			else
				escape.move_value = 160;
			escape.state = ESCAPE_FRONT_WAIT;
			bump_count+=2;
		break;
		case ESCAPE_FRONT_WAIT:			
			if(!escape.move) // Wait for movement to be completed
			{	
				escape.speed_left = ESCAPE_SPEED_ROTATE;
				if(bump_count > 3)
				{
					escape.move_value = 45; //100;
					escape.dir = RIGHT;
					bump_count = 0;
				}
				else 
				{
					escape.dir = LEFT;
					escape.move_value = 20; //70;
				}
				escape.rotate = true;
				escape.state = ESCAPE_WAIT_END;
			}
		break;
		case ESCAPE_LEFT:
			escape.speed_left = ESCAPE_SPEED_BWD;
			escape.dir 	= BWD;
			escape.move = true;
			if(bump_count > 3)
				escape.move_value = 190;
			else
				escape.move_value = 150;
			escape.state = ESCAPE_LEFT_WAIT;
			bump_count++;
		break;
		case ESCAPE_LEFT_WAIT:
			if(!escape.move) // Wait for movement to be completed
			{
				escape.speed_left = ESCAPE_SPEED_ROTATE;
				escape.dir = RIGHT;
				escape.rotate = true;
				if(bump_count > 3)
				{
					escape.move_value = 60; //110;
					bump_count = 0;
				}
				else
					escape.move_value = 20; //80;
				escape.state = ESCAPE_WAIT_END;
			}
		break;
		case ESCAPE_RIGHT:	
			escape.speed_left = ESCAPE_SPEED_BWD;
			escape.dir 	= BWD;
			escape.move = true;
			if(bump_count > 3)
				escape.move_value = 190;
			else
				escape.move_value = 150;
			escape.state = ESCAPE_RIGHT_WAIT;
			bump_count++;
		break;
		case ESCAPE_RIGHT_WAIT:
			if(!escape.move) // Wait for movement to be completed
			{ 
				escape.speed_left = ESCAPE_SPEED_ROTATE;		
				escape.dir = LEFT;
				escape.rotate = true;
				if(bump_count > 3)
				{
					escape.move_value = 60; //110;
					bump_count = 0;
				}
				else
					escape.move_value = 45; //80;
				escape.state = ESCAPE_WAIT_END;
			}
		break;
		case ESCAPE_WAIT_END:
			if(!(escape.move || escape.rotate)) // Wait for movement/rotation to be completed
				escape.state = IDLE;
		break;
	}
}

/**
 * Bumpers Event handler
 */
void bumpersStateChanged(void) {
	if(bumper_left && bumper_right) { // Both Bumpers were hit
        mywrite("   ESC: Front\n", 5);
        escape.state = ESCAPE_FRONT;
	}
	else if(bumper_left) { 			// Left Bumper was hit
		if(escape.state != ESCAPE_FRONT_WAIT) 
            mywrite("   ESC: Left\n", 6);
			escape.state = ESCAPE_LEFT;
	}
	else if(bumper_right) {			// Right Bumper was hit
		if(escape.state != ESCAPE_FRONT_WAIT)
            mywrite("   ESC: Right\n", 7);
			escape.state = ESCAPE_RIGHT;
	}
}

/*****************************************************************************/
// The new Avoid Behaviour:

// Some speed values for different movements:
#define AVOID_SPEED_L_ARC_LEFT  100 //30
#define AVOID_SPEED_L_ARC_RIGHT 100 //40 // 90
#define AVOID_SPEED_R_ARC_LEFT  100 //40 // 90
#define AVOID_SPEED_R_ARC_RIGHT 100 //30
#define AVOID_SPEED_ROTATE 	100 //30     // 60

// States for the Avoid FSM:
#define AVOID_OBSTACLE_RIGHT 		1
#define AVOID_OBSTACLE_LEFT 		2
#define AVOID_OBSTACLE_MIDDLE	    3
#define AVOID_OBSTACLE_MIDDLE_WAIT 	4
#define AVOID_END 					5
behaviour_command_t avoid = {0, 0, FWD, false, false, 0, IDLE};

/**
 * The new avoid behaviour. It uses the two ACS channels to avoid
 * collisions with obstacles. It drives arcs or rotates depending
 * on the sensor states and also behaves different after some
 * detecting cycles to avoid lock up situations. 
 */
void behaviour_avoid(void)
{
//  static uint8_t last_obstacle = LEFT;
	static uint8_t obstacle_counter = 0;
	switch(avoid.state)
	{
		case IDLE: 
		// This is different to the escape Behaviour where
		// we used the Event Handler to detect sensor changes...
		// Here we do this within the states!
			if(obstacle_right && obstacle_left) // left AND right sensor have detected something...
				avoid.state = AVOID_OBSTACLE_MIDDLE;
			else if(obstacle_left)  // Left "sensor" has detected something
				avoid.state = AVOID_OBSTACLE_LEFT;
			else if(obstacle_right) // Right "sensor" has detected something
				avoid.state = AVOID_OBSTACLE_RIGHT;
		break;
		case AVOID_OBSTACLE_MIDDLE:
			avoid.dir = FWD;
            avoid.rotate = 0;
            avoid.speed_left = 50;
            avoid.speed_right = 50;
			if(!(obstacle_left || obstacle_right))
			{
				if(obstacle_counter > 3)
				{
					obstacle_counter = 0;
					setStopwatch4(0);
				}
				else
					setStopwatch4(400);
				startStopwatch4();
				avoid.state = AVOID_END;
			}
		break;
		case AVOID_OBSTACLE_RIGHT:
			avoid.dir = FWD;
            avoid.rotate = 0;
            avoid.speed_left = 50;
            avoid.speed_right = 50;
			if(obstacle_right && obstacle_left)
				avoid.state = AVOID_OBSTACLE_MIDDLE;
			if(!obstacle_right)
			{
				setStopwatch4(500);
				startStopwatch4();
				avoid.state = AVOID_END;
			}
			obstacle_counter++;
		break;
		case AVOID_OBSTACLE_LEFT:
			avoid.dir = FWD;
            avoid.rotate = 0;
            avoid.speed_left = 50;
            avoid.speed_right = 50;
			if(obstacle_right && obstacle_left)
				avoid.state = AVOID_OBSTACLE_MIDDLE;
			if(!obstacle_left)
			{
				setStopwatch4(500); 
				startStopwatch4();
				avoid.state = AVOID_END;
			}
			obstacle_counter++;
		break;
		case AVOID_END:
			if(getStopwatch4() > 1000) // We used timing based movement here!
			{
				stopStopwatch4();
				setStopwatch4(0);
				avoid.state = IDLE;
			}
		break;
	}
}

/**
 * ACS Event Handler - ONLY used for LED display! 
 * This does not affect the behaviour at all! 
 * The sensor checks are completely done in the Avoid behaviour
 * statemachine.
 */
void acsStateChanged(void)
{
	if(obstacle_left && obstacle_right)
		statusLEDs.byte = 0b100100;
	else
		statusLEDs.byte = 0b000000;
	statusLEDs.LED5 = obstacle_left;
	statusLEDs.LED4 = (!obstacle_left);
	statusLEDs.LED2 = obstacle_right;
	statusLEDs.LED1 = (!obstacle_right);
	updateStatusLEDs();
}

/*****************************************************************************/
// Behaviour control:

/**
 * This function processes the movement commands that the behaviours generate. 
 * Depending on the values in the behaviour_command_t struct, it sets motor
 * speed, moves a given distance or rotates.
 */
void moveCommand(behaviour_command_t * cmd) {
    char text[55];
	if(cmd->move_value > 0) {  // move or rotate?
        
        if(cmd->rotate) {
            sprintf(text, "rotate: %d speed; %s dir;", cmd->speed_left, direction[cmd->dir]);
            mywrite(text, cmd->speed_left+cmd->speed_right+cmd->dir+1);
			rotate(cmd->speed_left, cmd->dir, cmd->move_value, false); 
        } else if(cmd->move) {
            sprintf(text, "  move: %d speed; %s dir;", cmd->speed_left, direction[cmd->dir]);
            mywrite(text, cmd->speed_left+cmd->speed_right+cmd->dir+2);
			move(cmd->speed_left, cmd->dir, DIST_MM(cmd->move_value), false); 
        }
		cmd->move_value = 0; // clear move value - the move commands are only
		                     // given once and then runs in background.
	}
	else if(!(cmd->move || cmd->rotate)) { // just move at speed? 
        sprintf(text, "  move: %d speed-left; %d speed-right; %s dir\n", cmd->speed_left, cmd->speed_right, direction[cmd->dir]);
        mywrite(text, cmd->speed_left+cmd->speed_right+cmd->dir+3);
		changeDirection(cmd->dir);
		moveAtSpeed(cmd->speed_left,cmd->speed_right);
	} else if(isMovementComplete()) { // movement complete? --> clear flags!
        mywrite(" -> movement complete\n", 4);
		cmd->rotate = false;
		cmd->move = false;
	}
}

/**
 * The behaviourController task controls the subsumption architechture. 
 * It implements the priority levels of the different behaviours. 
 */
void behaviourController(void) {
    // Call all the behaviour tasks:
	behaviour_cruise();
    behaviour_avoid();
	behaviour_escape();

    // Execute the commands depending on priority levels:
	if(escape.state != IDLE) // Highest priority - 3
		moveCommand(&escape);
	else if(avoid.state != IDLE) // Priority - 2
		moveCommand(&avoid);
	else if(cruise.state != IDLE) // Priority - 1
		moveCommand(&cruise); 
	else                     // Lowest priority - 0
		moveCommand(&STOP);  // Default command - do nothing! 
							 // In the current implementation this never 
							 // happens.
}

/*****************************************************************************/
// Main:

int main(void) {
	initRobotBase(); 

    writeString_P("\n\n             __.---.___\n");
    writeString_P("         ___/__\\_O_/___\\___\n");
    writeString_P("        /___\\__________/___\\\n");
    writeString_P("        |===|\\________/|===|\n");
    writeString_P("________|===|__________|===|________\n");
	writeString_P("    Pervasive Computing - Group 8\n");
	writeString_P("Simultaneous localization and mapping\n");
	writeString_P("    Jan - Felix - Tobias - Julian\n\n");

	setLEDs(0b111111);
	mSleep(2500);
	setLEDs(0b100100); 

	// Set Bumpers state changed event handler:
	BUMPERS_setStateChangedHandler(bumpersStateChanged);
	
	// Set ACS state changed event handler:
	ACS_setStateChangedHandler(acsStateChanged);
	
	powerON(); // Turn on Encoders, Current sensing, ACS and Power LED.
	setACSPwrMed(); 
	
	// Main loop
	while(true) {		
		behaviourController();
		task_RP6System();
	}
	return 0;
}
