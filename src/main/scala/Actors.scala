import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Actors {

  def Tank(name: String, capacity: Int, current: Int): Behavior[FactoryMsg] =
    Behaviors.receive { (context, msg) =>
      msg match {
        case Deposit(_, amount) =>
          val nextAmount = current + amount
          if (nextAmount < 0) {
            context.log.warn(s"[$name] Stock insuffisant ($current + $amount)")
            Behaviors.same
          } else if (nextAmount <= capacity) {
            context.log.info(s"[$name] Stock: $nextAmount/$capacity (ajout: $amount)")
            Tank(name, capacity, nextAmount)
          } else {
            context.log.error(s"BLOCAGE : Surplus dans $name !")
            Behaviors.same
          }
        case _ => Behaviors.same
      }
    }

  def Refinery(lourd: ActorRef[FactoryMsg], leger: ActorRef[FactoryMsg], gaz: ActorRef[FactoryMsg]): Behavior[FactoryMsg] =
    Behaviors.receiveMessage {
      case TickRefinery =>
        lourd ! Deposit(PetroleLourd, 25)
        leger ! Deposit(PetroleLeger, 45)
        gaz   ! Deposit(PetroleGaz, 55)
        Behaviors.same
      case _ => Behaviors.same
    }

  def Cracker(inputTank: ActorRef[FactoryMsg], 
              outputTank: ActorRef[FactoryMsg], 
              inRes: Resource, 
              outRes: Resource, 
              inQty: Int, 
              outQty: Int): Behavior[FactoryMsg] =
    Behaviors.receiveMessage {
      case m if m == TickCracker1 || m == TickCracker2 =>
        inputTank ! Deposit(inRes, -inQty)
        outputTank ! Deposit(outRes, outQty)
        Behaviors.same
      case _ => Behaviors.same
    }
}