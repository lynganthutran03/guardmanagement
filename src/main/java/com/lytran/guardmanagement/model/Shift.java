package com.lytran.guardmanagement.model;

import java.time.LocalDate;

import com.lytran.guardmanagement.entity.Guard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "shifts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"shift_date", "time_slot", "block"})
})
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guard_id")
    private Guard guard;

    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_slot", nullable = false)
    private TimeSlot timeSlot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Block block;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Guard getGuard() {
        return guard;
    }

    public void setGuard(Guard guard) {
        this.guard = guard;
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }
}
