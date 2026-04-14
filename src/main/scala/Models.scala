import akka.actor.typed.ActorRef

sealed trait Resource
case object PetroleLourd extends Resource
case object PetroleLeger extends Resource
case object PetroleGaz extends Resource

sealed trait FactoryMsg

case object TickRefinery extends FactoryMsg
case object TickCracker1 extends FactoryMsg
case object TickCracker2 extends FactoryMsg
case class Deposit(res: Resource, amount: Int) extends FactoryMsg