import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Actors {

  def InactiveTank(name: String, capacity: Int, current: Int): Behavior[FactoryMsg] =
    Behaviors.receive { (context, msg) =>
      msg match {
        case InitTank(refineryRef) =>
          Tank(name, capacity, current, refineryRef)
        case _ => Behaviors.same
      }
    }

  def Tank(name: String, capacity: Int, current: Int, refinery: ActorRef[FactoryMsg]): Behavior[FactoryMsg] =
    Behaviors.receive { (context, msg) =>
      msg match {
        case Deposit(_, amount, replyTo) =>
          val nextAmount = current + amount
          if (nextAmount < 0) {

            if (replyTo != context.system.deadLetters) {
              replyTo ! ActionStatus(false, amount)
            }

            val status = f"[${SimulationClock.getRelativeTime}%-7s] [$name%-10s] Stock insuffisant ($current + $amount)"
            println(status)

            Behaviors.same

          } else if (nextAmount <= capacity) {

            if (replyTo != context.system.deadLetters) {
              replyTo ! ActionStatus(true, amount)
            }

            val status = f"[${SimulationClock.getRelativeTime}%-7s] [$name%-10s] Stock: $nextAmount/$capacity | ajout: $amount"
            println(status)

            Tank(name, capacity, nextAmount, refinery)
          } else {

            refinery ! TankStatus(true) // Informer la raffinerie du blocage

            val status = f"[${SimulationClock.getRelativeTime}%-7s] [$name%-10s] BLOCAGE : Surplus dans $name !"
            println(status)

            Behaviors.same
          }
        case _ => Behaviors.same
      }
    }

  def Refinery(lourd: ActorRef[FactoryMsg], 
             leger: ActorRef[FactoryMsg], 
             gaz: ActorRef[FactoryMsg], 
             running: Boolean = true): Behavior[FactoryMsg] =
  Behaviors.receive { (context, message) =>
    message match {
      case TankStatus(full) =>
        Refinery(lourd, leger, gaz, !full)
        
      case TickRefinery if running =>
        lourd ! Deposit(PetroleLourd, 25, context.system.deadLetters)
        leger ! Deposit(PetroleLeger, 45, context.system.deadLetters)
        gaz ! Deposit(PetroleGaz, 55, context.system.deadLetters)
        Behaviors.same
        
      case TickRefinery =>
        // On ignore si running == false
        Behaviors.same
        
      case _ => Behaviors.same
    }
  }

  def Cracker(inputTank: ActorRef[FactoryMsg], 
              outputTank: ActorRef[FactoryMsg], 
              inRes: Resource, 
              outRes: Resource, 
              inQty: Int, 
              outQty: Int): Behavior[FactoryMsg] =
    Behaviors.setup { context =>
      
      val replyTo = context.messageAdapter[ActionStatus](identity)

      def active(): Behavior[FactoryMsg] = Behaviors.receiveMessage {
        case m if m == TickCracker1 || m == TickCracker2 =>
          // On demande le retrait au tank d'entrée
          inputTank ! Deposit(inRes, -inQty, replyTo) 
          Behaviors.same

        case ActionStatus(true, _) =>
          // Le retrait a réussi, on peut produire
          outputTank ! Deposit(outRes, outQty, context.system.deadLetters)
          Behaviors.same

        case ActionStatus(false, _) =>
          // Le retrait a échoué (stock insuffisant), on ne produit rien
          Behaviors.same

        case _ => Behaviors.same
      }
      active()
    }
}