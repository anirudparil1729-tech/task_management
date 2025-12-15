from datetime import datetime, timedelta

from services.recurring import (
    expand_recurring_task,
    get_next_occurrence,
    parse_simple_recurrence,
)


def test_parse_simple_daily_recurrence():
    base_date = datetime(2024, 1, 1, 10, 0, 0)
    occurrences = parse_simple_recurrence("daily", base_date)

    assert len(occurrences) == 30
    assert occurrences[0] == base_date
    assert occurrences[1] == base_date + timedelta(days=1)
    assert occurrences[7] == base_date + timedelta(days=7)


def test_parse_simple_weekly_recurrence():
    base_date = datetime(2024, 1, 1, 10, 0, 0)
    occurrences = parse_simple_recurrence("weekly", base_date)

    assert len(occurrences) == 12
    assert occurrences[0] == base_date
    assert occurrences[1] == base_date + timedelta(weeks=1)
    assert occurrences[4] == base_date + timedelta(weeks=4)


def test_parse_simple_monthly_recurrence():
    base_date = datetime(2024, 1, 15, 10, 0, 0)
    occurrences = parse_simple_recurrence("monthly", base_date)

    assert len(occurrences) == 12
    assert occurrences[0].day == 15
    assert occurrences[0].month == 1
    assert occurrences[1].month == 2
    assert occurrences[11].month == 12


def test_expand_recurring_task_daily():
    start_date = datetime(2024, 1, 1, 10, 0, 0)
    end_date = datetime(2024, 1, 5, 10, 0, 0)

    occurrences = expand_recurring_task("FREQ=DAILY", start_date, end_date, limit=10)

    assert len(occurrences) <= 5


def test_expand_recurring_task_with_limit():
    start_date = datetime(2024, 1, 1, 10, 0, 0)

    occurrences = expand_recurring_task("FREQ=DAILY", start_date, limit=5)

    assert len(occurrences) <= 5


def test_get_next_occurrence():
    after_date = datetime(2024, 1, 1, 10, 0, 0)

    next_occurrence = get_next_occurrence("FREQ=DAILY", after_date)

    if next_occurrence:
        assert next_occurrence > after_date


def test_invalid_recurrence_rule():
    start_date = datetime(2024, 1, 1, 10, 0, 0)

    occurrences = expand_recurring_task("INVALID_RULE", start_date, limit=5)

    assert occurrences == []


def test_empty_recurrence_rule():
    start_date = datetime(2024, 1, 1, 10, 0, 0)

    occurrences = expand_recurring_task("", start_date, limit=5)

    assert occurrences == []
