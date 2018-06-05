package nl.utwente.simulator.entities;

/**
 * Chain end non-crosslinker reactive centers can be either a reactive center on a(non-crosslinker) monomer or a reactive center on a half-initiator
 *
 *  I• and  ^^^^M•
 *
 * Chain end crosslinker reactive centers are reactive centers on crosslinker molecules of which only one side is in a polymer chain
 *
 *
 *  ^^^^C•
 *      |
 *      C=
 *
 * Mid-chain reactive centers are reactive centers on crosslinker molecules of which both sides are in a polymer chain
 *
 *  ^^^^C•     and  ^^^^C•
 *      |               |
 *  ^^^^C^^^^       ^^^^C•
 *
 */
public enum RadicalPosition{
    MID_CHAIN_CROSSLINKER,
    CHAIN_END_NON_CROSSLINKER,
    CHAIN_END_CROSSLINKER
}