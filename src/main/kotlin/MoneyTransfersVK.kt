const val TOTAL_IN_ROUBLE = 100.0
const val DAY_LIMIT_CARDS = 150_000.0 * TOTAL_IN_ROUBLE
const val DAY_LIMIT_VKPAY = 15_000.0 * TOTAL_IN_ROUBLE
const val PERCENT_VISA_MIR = 0.0075
const val MIN_TAX_VISA_MIR = 35.0 * TOTAL_IN_ROUBLE
const val MAX_SUM_PER_MONTH_MASTERCARD_MAESTRO = 75_000.0 * TOTAL_IN_ROUBLE
const val PERCENT_MASTERCARD_MAESTRO = 0.006
const val FIX_TAX_MASTERCARD_MAESTRO = 20.0 * TOTAL_IN_ROUBLE

enum class CardOrAccountType { VKPay, MasterCardMaestro, VisaMir }

/*
Для упрощения проверяем только лимит переводов в сутки.
Причем для карт - по всем картам в сумме, а не по отдельности.
 */
fun main(args: Array<String>) {
    var totalSumInMonthCards = 0.0
    var totalSumInMonthVKPay = 0.0
    var limitExceeded = false
    var errorText = ""
    while (true) {
        print("-= Тип карты/счета =-\n1) VK Pay\n2) MasterCard/Maestro\n3) Visa/МИР\n0) Выход\nВведите номер: ")
        val accountType = readln().toInt();
        if (accountType == 0) {
            println("Программа завершена.")
            break
        }
        val selectedCardOrAccount = when (accountType) {
            1 -> CardOrAccountType.VKPay
            2 -> CardOrAccountType.MasterCardMaestro
            3 -> CardOrAccountType.VisaMir
            else -> CardOrAccountType.VKPay
        }
        print("Введите сумму перевода (руб.): ")
        val transferSum = readln().toDouble() * TOTAL_IN_ROUBLE
        if (selectedCardOrAccount == CardOrAccountType.VKPay) {
            totalSumInMonthVKPay += transferSum
            val totalSumInMonthVKPayRub = totalSumInMonthVKPay / TOTAL_IN_ROUBLE
            val dayLimitVKPayRub = DAY_LIMIT_VKPAY / TOTAL_IN_ROUBLE
            limitExceeded = totalSumInMonthVKPay > DAY_LIMIT_VKPAY
            errorText =
                "Невозможно сделать перевод со счета VK Pay: исчерпан лимит на сутки.\nС учетом запрошенной суммы перевода сумма переводов за сутки составит $totalSumInMonthVKPayRub руб. Лимит: $dayLimitVKPayRub руб.\n\n"
            if (limitExceeded) totalSumInMonthVKPay -= transferSum
        } else {
            totalSumInMonthCards += transferSum
            val totalSumInMonthCardsRub = totalSumInMonthCards / TOTAL_IN_ROUBLE
            val dayLimitCardsyRub = DAY_LIMIT_CARDS / TOTAL_IN_ROUBLE
            limitExceeded = totalSumInMonthCards > DAY_LIMIT_CARDS
            errorText =
                "Невозможно сделать перевод с карты: исчерпан лимит на сутки.\nС учетом запрошенной суммы перевода сумма переводов за сутки составит $totalSumInMonthCardsRub руб. Лимит: $dayLimitCardsyRub руб.\n\n"
            if (limitExceeded) totalSumInMonthCards -= transferSum
        }

        if (!limitExceeded) {
            val taxSum = if (selectedCardOrAccount == CardOrAccountType.VKPay)
                transferFeeCalculation(transferSum, totalSumInMonthVKPay)
            else
                transferFeeCalculation(transferSum, totalSumInMonthCards, selectedCardOrAccount)

            val taxSumRub = taxSum / TOTAL_IN_ROUBLE
            println("Комиссия за перевод составит $taxSumRub руб.\n\n")
        } else {
            println(errorText)
        }
    }
}

fun transferFeeCalculation(
    transferSum: Double,
    totalSum: Double = 0.0,
    accountType: CardOrAccountType = CardOrAccountType.VKPay
) = when (accountType) {
    CardOrAccountType.MasterCardMaestro ->
        if (totalSum > MAX_SUM_PER_MONTH_MASTERCARD_MAESTRO) transferSum * PERCENT_MASTERCARD_MAESTRO + FIX_TAX_MASTERCARD_MAESTRO else 0.0
    CardOrAccountType.VisaMir ->
        if (transferSum * PERCENT_VISA_MIR <= MIN_TAX_VISA_MIR) MIN_TAX_VISA_MIR else transferSum * PERCENT_VISA_MIR
    else -> 0.0
}
