#include "RP6RobotBaseLib.h"

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

void task_UART(void)
{
    uint8_t charsToReceive = 1;

    char receiveBuffer[charsToReceive+1];
    clearReceptionBuffer();
    uint8_t cnt;
    for(cnt = 0; cnt < charsToReceive; cnt++) {
        receiveBuffer[cnt] = 0;
    }
    uint8_t buffer_pos = 0;
    while(true)
    {
        if(getBufferLength()) {
            receiveBuffer[buffer_pos] = readChar(); // get next character from reception buffer
            if(receiveBuffer[buffer_pos]=='\n') // End of line detected!
            {
                receiveBuffer[buffer_pos]='\0'; // Terminate String with a 0, so other routines.
                buffer_pos = 0;                 // can determine where it ends!
                                                // We also overwrite the Newline character here.
                break; // We are done and can leave reception loop!
            }
            else if(buffer_pos >= charsToReceive) // IMPORTANT: We can not receive more
            {                                     // characters than "charsToReceive" because
                                                  // our buffer wouldn't be large enough!
                receiveBuffer[charsToReceive]='\0'; // So if we receive more characters, we just
                                                 // stop reception and terminate the String.
                writeString_P("\n\nYou entered more characters than possible!\n");
                break; // We are done and can leave reception loop!
            }
            buffer_pos++;
        }
        task_Bumpers(); // ständig Bumper auslesen
        task_ACS(); // ständig ACS auslesen (Anti Collision System)
    }
    //if(buffer_pos > 0)
    {
        writeChar('\n');
        for(cnt = 0; cnt < charsToReceive; cnt++) {
            writeInteger(receiveBuffer[cnt],DEC);
            writeChar(',');
        }
        writeInteger(receiveBuffer[charsToReceive],DEC);
        writeString_P("\n");

        writeString_P("-> \"");
        writeString(receiveBuffer); // Output the received data as a String
        writeString_P("\" !\n");

    }
}

int main(void)
{
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

    // Event Handler registrieren:
    ACS_setStateChangedHandler(acsStateChanged);
    BUMPERS_setStateChangedHandler(bumpersStateChanged);

    powerON(); // Turn on Encoders, Current sensing, ACS and Power LED.
    //setACSPwrOff();
    //setACSPwrLow(); 
    setACSPwrMed(); // ACS auf mittlere Sendeleistung stellen.
    //setACSPwrHigh();

    while(true) 
    {
        task_UART(); // (Universal Asynchronous Receiver/Transmitter)
    }
    return 0;
}

