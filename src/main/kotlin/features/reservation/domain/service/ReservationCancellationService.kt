package com.martdev.features.reservation.domain.service

import com.martdev.features.payment.domain.service.PaymentService
import com.martdev.features.reservation.domain.model.Reservation
import com.martdev.features.reservation.domain.model.ReservationStatus
import org.koin.core.annotation.Single

interface ReservationCancellationService {
    /**
     * Cancel a reservation as an admin. If the reservation is CONFIRMED, the underlying
     * successful payment is refunded via Paystack before the status flips. If the refund
     * fails the cancellation is aborted so the financial state matches the booking state.
     */
    suspend fun cancelByAdmin(id: Long): Reservation
}

@Single
class ReservationCancellationServiceImpl(
    private val reservationService: ReservationService,
    private val paymentService: PaymentService,
) : ReservationCancellationService {

    override suspend fun cancelByAdmin(id: Long): Reservation {
        val reservation = reservationService.getReservationById(id)
        if (reservation.status == ReservationStatus.CONFIRMED) {
            paymentService.refundPaymentForReservation(id)
        }
        return reservationService.cancelReservationAdmin(id)
    }
}
