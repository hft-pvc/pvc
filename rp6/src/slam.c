#include "RP6RobotBaseLib.h"

#define IDLE 0
#define MOVE 1
#define MOVE_IDLE 7

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

const char* direction[4] = {"FWD", "BWD", "LEFT", "RIGHT"};

void acsStateChanged(void)
{
    writeString_P("ACS status hat sich geändert!   L: ");
    if(obstacle_left)  // Hindernis links
        writeChar('o');
    else
        writeChar(' ');
    writeString_P(" | R: ");
    if(obstacle_right) // Hindernis rechts
        writeChar('o');
    else
        writeChar(' ');
    if(obstacle_left && obstacle_right) // Mitte?
        writeString_P("   MITTE!");
    writeChar('\n');
    statusLEDs.LED6 = obstacle_left && obstacle_right; // Mitte?
    statusLEDs.LED3 = statusLEDs.LED6;
    statusLEDs.LED5 = obstacle_left;     // Hindernis links
    statusLEDs.LED4 = (!obstacle_left);  // LED5 invertiert!
    statusLEDs.LED2 = obstacle_right;    // Hindernis rechts
    statusLEDs.LED1 = (!obstacle_right); // LED2 invertiert!
    updateStatusLEDs(); 
} 

void bumpersStateChanged(void)
{
    writeString_P("\nBumper Status hat sich geaendert:\n");
    if(bumper_left) 
        writeString_P(" - Linker Bumper gedrueckt!\n");
    else
        writeString_P(" - Linker Bumper nicht gedrueckt.\n");
    if(bumper_right)
        writeString_P(" - Rechter Bumper gedrueckt!\n");
    else
        writeString_P(" - Rechter Bumper nicht gedrueckt.\n");
}

void moveCommand(behaviour_command_t* cmd) {
    if(cmd->move_value > 0) {
        if (cmd->rotate) {
            rotate(cmd->speed_left, cmd->dir, cmd->move_value, false);
        } else if (cmd->move) {
            move(cmd->speed_left, cmd->dir, DIST_MM(cmd->move_value), false);
        }
        // clear move value - the move commands are only given once 
        // and then runs in background
        cmd->move_value = 0; 
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
        if(a == 10)
            return;

        a -= 48;
        if(a>9)
            return;
        switch(a)
        {
            case 0:
                writeString_P("IDLE\n");
                ext.state = IDLE;
                break;
            case 1:
                writeString_P("STOP\n");
                ext.speed_left = 0;
                ext.speed_right = 0;
                ext.move = false;
                ext.state = MOVE;
                break;
            case 2:
                writeString_P("FWD\n");
                ext.speed_left = 50;
                ext.speed_right = 50;
                ext.dir = FWD;
                ext.move = true;
                ext.move_value = 20;
                ext.state = MOVE;
                setStopwatch4(400);
                startStopwatch4();
                idle.state = MOVE_IDLE;
                break;
            case 3:
                writeString_P("BWD\n");
                ext.speed_left = 50;
                ext.speed_right = 50;
                ext.dir = BWD;
                ext.move = true;
                ext.state = MOVE;
                break;
            case 4:
                writeString_P("LEFT\n");
                ext.speed_left = 50;
                ext.dir = LEFT;
                ext.rotate = true;
                ext.state = MOVE;
                break;
            case 5:
                writeString_P("RIGHT\n");
                ext.speed_left = 50;
                ext.dir = RIGHT;
                ext.rotate = true;
                ext.state = MOVE;
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
    }
}

void behaviourController(void) {
    // Call all the behaviour tasks:
    behaviour_ext();
    behaviour_idle();

    if (ext.state != IDLE) {
        writeString_P("I'm moving");
        moveCommand(&ext);
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

