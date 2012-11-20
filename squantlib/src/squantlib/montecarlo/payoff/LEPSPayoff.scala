package squantlib.montecarlo.payoff

import squantlib.setting.initializer.Currencies
import scala.collection.mutable.{Map => mutableMap}
import scala.collection.JavaConversions._
import org.codehaus.jackson.JsonNode
import org.codehaus.jackson.map.ObjectMapper

/**
 * Interprets JSON formula specification for sum of linear formulas with discrete range.
 * JSON format:
 * - {type:"leps", variable:string, payoff:formula}, where
 *   formula = Array {minrange:double, maxrange:double, mult:double, add:double}
 *   payment for array(i) is 
 *     if minrange(i) <= X < maxrange(i) => mult(i) * variable + add(i)
 *     otherwise zero
 */
class LEPS1dPayoff(val formula:String) extends Payoff {
  
	val mapper = new ObjectMapper
	val node = mapper.readTree(formula)
	val variable:String = node.get("variable").getTextValue
	val LEPSformula = LEPS1dFormula(node.get("payoff"))
  
	val variables:Set[String] = Set(variable)
	 
	override def price(fixings:Map[String, Double]):Option[Double] = 
	  if (fixings.contains(variable)) price(fixings(variable))
	  else None
	
	override def price(fixing:Double):Option[Double] = Some(LEPSformula.price(fixing))
	
	override def toString:String = LEPSformula.toString
	
}


/**
 * Interprets JSON formula for series of LEPS1dPayoffs.
 * JSON format:
 * - {type:"lepsseries", variable:string, payoff:Array[formula]}, where
 *   formula = Array[{minrange:double, maxrange:double, mult:double, add:double}]
 *   payment for array(i) is 
 *     if minrange(i) <= X < maxrange(i) => mult(i) * variable + add(i)
 *     otherwise zero
 */
class LEPS1dPayoffSeries(val formula:String) extends PayoffSeries {
  
	val mapper = new ObjectMapper
	val node = mapper.readTree(formula)
	val variable:String = node.get("variable").getTextValue
	val payoffs:List[LEPS1dFormula] = node.get("payoff").getElements.map(LEPS1dFormula).toList
	val paycount = payoffs.size
  
	val variables:Set[String] = Set(variable)
	
	override def price(fixings:List[Double])(implicit d:DummyImplicit):List[Option[Double]] = {
	  assert(fixings.size == paycount)
	  if (payoffs.isEmpty) List.empty
	  else (for (i <- 0 to paycount - 1) yield (Some(payoffs(i).price(fixings(i))))).toList
	}
	
	override def price(fixings:List[Map[String, Double]]):List[Option[Double]] = {
	  assert(fixings.size == paycount && fixings.forall(_.contains(variable)))
	  price(fixings.map(_(variable)))
	}
	
	override def price:List[Option[Double]] = List.empty
	 
	override def toString:String = payoffs.toString
	
}


case class LEPS1dFormula (val node:JsonNode) {
  
	val formula:Array[LEPS1dComponent] = node.getElements.map(LEPS1dComponent).toArray
	
	def price(fixing:Double):Double = formula.map(_.price(fixing)).sum
}


case class LEPS1dComponent (val subnode:JsonNode) {
	
	private def getvalue(name:String):Option[Double] = 
	  if (subnode has name) 
	    subnode.get(name) match {
	      case n if n.isNumber => Some(n.getDoubleValue)
	      case n if n.getTextValue.endsWith("%") => try {Some(n.getTextValue.dropRight(1).toDouble / 100)} catch { case _ => Some(Double.NaN)}
	      case _ => Some(Double.NaN)
	    }
	  else None
	
	val minRange:Option[Double] = getvalue("minrange")
	val maxRange:Option[Double] = getvalue("maxrange")
	val coeff:Option[Double] = getvalue("mult")
	val constant:Option[Double] = getvalue("add")
	 
	def price(fixing:Double):Double = {
	  minRange match {
	    case Some(f) if fixing < f => return 0.0
	    case _ =>
	  }
	  
	  maxRange match {
	    case Some(c) if fixing >= c => return 0.0
	    case _ =>
	  }
	   
	  (coeff, constant) match {
	    case (None, None) => 0.0
		case (None, Some(c)) => c
		case (Some(x), None) => x * fixing
		case (Some(x), Some(c)) => x * fixing + c
	  }
	}
	
}


