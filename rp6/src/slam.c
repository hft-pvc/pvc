#include "RP6RobotBaseLib.h"

#define IDLE 0
#define MOVE 1
#define MOVE_IDLE 7
#define NEXT_FWD 8

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

#define CRUISE_SPEED_FWD    80 //40 // 100 Default speed when no obstacles are detected!
behaviour_command_t cruise = {CRUISE_SPEED_FWD, CRUISE_SPEED_FWD, FWD, false, false, 0, MOVE};

unsigned isAuto = false;
const char* direction[4] = {"FWD", "BWD", "LEFT", "RIGHT"};
int lastState = 0;
uint16_t last_dist = 0;
unsigned isMoving = false;

void mywrite(const char *pstring, int state)
{
    if (state != lastState)
    {
        lastState = state;
        uint8_t c;
        for (;(c = *pstring++);writeChar(c));
    }
}

void acsStateChanged(void)
{
    statusLEDs.LED6 = obstacle_left && obstacle_right; // Mitte?
    statusLEDs.LED3 = statusLEDs.LED6;
    statusLEDs.LED5 = obstacle_left;     // Hindernis links
    statusLEDs.LED4 = (!obstacle_left);  // LED5 invertiert!
    statusLEDs.LED2 = obstacle_right;    // Hindernis rechts
    statusLEDs.LED1 = (!obstacle_right); // LED2 invertiert!
    updateStatusLEDs(); 
} 

void moveCommand(behaviour_command_t* cmd) {
    if(cmd->move_value > 0) {
        if (cmd->rotate) {
            rotate(cmd->speed_left, cmd->dir, cmd->move_value, false);
        } else if (cmd->move) {
            last_dist = DIST_MM(cmd->move_value); // store the used move_value for late evaluation
            isMoving = true;
            move(cmd->speed_left, cmd->dir, DIST_MM(cmd->move_value), false);
        }
        // clear move value - the move commands are only given once 
        // and then runs in background
        cmd->move_value = 0; 
        cmd->speed_left = 0;
    }
    else if (!(cmd->move || cmd->rotate)) {
        changeDirection(cmd->dir);
        moveAtSpeed(cmd->speed_left, cmd->speed_right);
    }
    else if (isMovementComplete()) { // movement complete? -> clear flags!
        cmd->rotate = false;
        cmd->move = false;
    }
}


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
            writeString_P("3\n");
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
                    writeString_P("1");
					escape.move_value = 90; //100;
					escape.dir = RIGHT;
					bump_count = 0;
				}
				else 
				{
                    writeString_P("0");
					escape.dir = LEFT;
					escape.move_value = 90; //70;
				}
				escape.rotate = true;
				escape.state = ESCAPE_WAIT_END;
			}
		break;
		case ESCAPE_LEFT:
            writeString_P("3\n");
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
                writeString_P("1\n");
				escape.speed_left = ESCAPE_SPEED_ROTATE;
				escape.dir = RIGHT;
				escape.rotate = true;
				if(bump_count > 3)
				{
					escape.move_value = 90; //110;
					bump_count = 0;
				}
				else
					escape.move_value = 90; //80;
				escape.state = ESCAPE_WAIT_END;
			}
		break;
		case ESCAPE_RIGHT:	
            writeString_P("3\n");
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
                writeString_P("0\n");
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
			if(!(escape.move || escape.rotate)) { // Wait for movement/rotation to be completed
                writeString_P("2\n");
				escape.state = IDLE;
            }
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

/* Behaviour for external commands comming either from console or GUI */
#define EXTERN_SPEED_L_ARC_LEFT  100
#define EXTERN_SPEED_L_ARC_RIGHT 100
#define EXTERN_SPEED_R_ARC_LEFT  100
#define EXTERN_SPEED_R_ARC_RIGHT 100
#define EXTERN_SPEED_ROTATE      100

behaviour_command_t ext = {0, 0, FWD, false, false, 0, IDLE};
behaviour_command_t idle = {0, 0, FWD, false, false, 0, IDLE};

void behaviour_ext(void) {
    if(getBufferLength())
    {
        uint8_t a = readChar();
        if(a == 10) return; // Ignore line-feed (LF)

        a -= 48;

        if(a>9) return; // Only accept codes 0-9

        switch(a)
        {
            case 0: // IDLE
                writeString_P("4\n");
                ext.state = IDLE;
                break;
            case 1: // STOP
                writeString_P("5\n");
                // Stop everything and disable the auto mode
                
                isAuto = false;
                
                ext.speed_left = 0;
                ext.speed_right = 0;
                ext.move = false;
                ext.state = MOVE;

                avoid.speed_left = 0;
                avoid.speed_right = 0;
                avoid.move = false;
                avoid.state = IDLE;

                escape.speed_left = 0;
                escape.speed_right = 0;
                escape.move = false;
                escape.state = IDLE;  

                cruise.speed_left = 0;
                cruise.speed_right = 0;
                cruise.move = false;
                cruise.state = IDLE;  
                break;
            case 2: // FWD
                writeString_P("2\n");
                ext.speed_left = 50;
                ext.speed_right = 50;
                ext.dir = FWD;
                ext.move = true;
                ext.move_value = 300; // 30cm
                ext.state = MOVE;
                
                // Make the robot stop afterwards
                setStopwatch4(400);
                startStopwatch4();
                idle.state = MOVE_IDLE;
                break;
            case 3: // BWD
                writeString_P("3\n");
                ext.speed_left = 50;
                ext.speed_right = 50;
                ext.dir = BWD;
                ext.move_value = 300; // 30cm
                ext.move = true;
                ext.state = MOVE;

                // Make the robot stop afterwards
                setStopwatch4(400);
                startStopwatch4();
                idle.state = MOVE_IDLE;
                break;
            case 4: // LEFT
                writeString_P("5\n");
                writeString_P("0\n");
                ext.speed_left = 50;
                ext.dir = LEFT;
                ext.rotate = true;
                ext.move_value = 90;
                ext.state = MOVE;

                // Make the robot stop afterwards
                setStopwatch4(400);
                startStopwatch4();
                idle.state = NEXT_FWD;
                break;
            case 5: // RIGHT
                writeString_P("1\n");
                ext.speed_left = 50;
                ext.dir = RIGHT;
                ext.rotate = true;
                ext.move_value = 90;
                ext.state = MOVE;

                // Make the robot stop afterwards
                setStopwatch4(400);
                startStopwatch4();
                idle.state = NEXT_FWD;
                break;
            case 6:
                // Toggle Auto-Mode
                isAuto = !isAuto;

                if (isAuto)
                    writeString_P("2\n");
                // initial cruise settings
                cruise.speed_left = CRUISE_SPEED_FWD;
                cruise.speed_right = CRUISE_SPEED_FWD;
                cruise.dir = FWD;
                cruise.rotate = false;
                cruise.move = false;
                cruise.move_value = 0;
                cruise.state = MOVE; 

                break;
        }
    }
}

void behaviour_idle(void) {
    switch(idle.state) {
        case MOVE_IDLE:
            if (getStopwatch4() > 1000) {
                stopStopwatch4();
                setStopwatch4(0);
                ext.state = IDLE;
            }
        break;
        case NEXT_FWD:
            if (!ext.rotate) {
                writeString_P("2\n");
                ext.speed_left = 50;
                ext.speed_right = 50;
                ext.dir = FWD;
                ext.move = true;
                ext.move_value = 300; // 30cm
                ext.state = MOVE;
                
                // Make the robot stop afterwards
                setStopwatch4(400);
                startStopwatch4();
                idle.state = MOVE_IDLE;
            }
        break;
    }
}


void behaviourController(void) {

    // Call all the behaviour tasks for auto-mode
    if (isAuto) {
        behaviour_avoid();
        behaviour_escape();

        // Execute the commands depending on priority levels:
        if(escape.state != IDLE) // Highest priority - 3
            moveCommand(&escape);
        else if(avoid.state != IDLE) // Priority - 2
            moveCommand(&avoid);
        else if (cruise.state != IDLE) // Priority - 1
            moveCommand(&cruise);
    } 

    // Always check for external commands
    behaviour_ext();
    behaviour_idle();

    if (ext.state != IDLE)
        moveCommand(&ext);

    // Send an appropriate stop command
    if (mleft_dist >= last_dist && isMoving) {
        isMoving = false;
        writeString_P("5\n");
    }

}

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
    mSleep(1000);
    setLEDs(0b001001);

    // Set ACS state changed event handler: 
    ACS_setStateChangedHandler(acsStateChanged);

    // Set Bumpers state changed event handler:
    BUMPERS_setStateChangedHandler(bumpersStateChanged);

    powerON(); // Turn on Encoders, Current sensing, ACS and Power LED.
    //setACSPwrOff();
    //setACSPwrLow(); 
    setACSPwrMed(); // ACS auf mittlere Sendeleistung stellen.
    //setACSPwrHigh();

    while(true)
    {
        behaviourController();
        task_RP6System();
    }
    return 0;
}

