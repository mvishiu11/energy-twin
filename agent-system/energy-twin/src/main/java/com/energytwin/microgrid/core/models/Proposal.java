package com.energytwin.microgrid.core.models;

import jade.core.AID;
import lombok.Data;

@Data
public class Proposal {
  AID sender;
  double amount;
  double cost;
  int arrivalIndex; // tie-break
  double acceptedAmount;
}
