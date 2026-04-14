import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import java.time.Duration as JDuration

// Clock basique pour afficher le temps écoulé depuis le début de la simulation
object SimulationClock {
  val startTime: Long = System.currentTimeMillis()

  def getRelativeTime: String = {
    val elapsed = System.currentTimeMillis() - startTime
    val seconds = elapsed / 1000.0
    f"${seconds+2}%2.1fs"
  }
}


object Main extends App {
  def apply(): Behavior[Unit] = Behaviors.setup { context =>
    
    val tankLourd = context.spawn(Actors.InactiveTank("tankLourd", 500, 0), "tankLourd")
    val tankLeger = context.spawn(Actors.InactiveTank("tankLeger", 500, 0), "tankLeger")
    val tankGas   = context.spawn(Actors.InactiveTank("tankGas", 500, 0), "tankGas")

    val refinery = context.spawn(Actors.Refinery(tankLourd, tankLeger, tankGas), "Refinery")

    tankLourd ! InitTank(refinery)
    tankLeger ! InitTank(refinery)
    tankGas   ! InitTank(refinery)

    val cracker1 = context.spawn(Actors.Cracker(tankLourd, tankLeger, PetroleLourd, PetroleLeger, 40, 30), "Cracker1")
    val cracker2 = context.spawn(Actors.Cracker(tankLeger, tankGas, PetroleLeger, PetroleGaz, 30, 20), "Cracker2")



    // Raffinerie toutes les 5s
    context.system.scheduler.scheduleWithFixedDelay(
      JDuration.ofSeconds(5), JDuration.ofSeconds(5), 
      () => refinery ! TickRefinery, context.executionContext)

    // Craqueurs toutes les 2s
    context.system.scheduler.scheduleWithFixedDelay(
      JDuration.ofSeconds(2), JDuration.ofSeconds(2), 
      () => cracker1 ! TickCracker1, context.executionContext)

    context.system.scheduler.scheduleWithFixedDelay(
      JDuration.ofSeconds(2), JDuration.ofSeconds(2), 
      () => cracker2 ! TickCracker2, context.executionContext)

    Behaviors.empty
  }

  val system = ActorSystem(apply(), "FactorioSystem")
 // Attendre une entrée pour terminer le système
  scala.io.StdIn.readLine() 
  system.terminate()
}