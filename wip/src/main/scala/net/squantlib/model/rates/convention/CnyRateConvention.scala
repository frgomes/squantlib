package net.squantlib.model.rates.convention

import org.jquantlib.time.Period
import org.jquantlib.daycounters.Actual365Fixed
import org.jquantlib.currencies.Asia.CNYCurrency

class CnyRateConvention extends RateConvention{
  import org.jquantlib.currencies.Asia.CNYCurrency
  
  	val currency = new CNYCurrency

  	val useRateDiscount = false
	def iborindex(p:Period) = null
	val swapFloatIndex = null
	val swapFixDaycount = null
	val swapFixPeriod = null

	val useFXdiscount = true
	val swapPointMultiplier = 1.0

	override val useNDSdiscount = false
	override val ndsFixDaycount = new Actual365Fixed
}


