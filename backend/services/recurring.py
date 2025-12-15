from datetime import datetime, timedelta

from dateutil.rrule import DAILY, MONTHLY, WEEKLY, rrule, rrulestr

from utils import utcnow


def parse_recurrence_rule(rule_string: str) -> rrule | None:
    try:
        if rule_string.upper().startswith("RRULE:"):
            return rrulestr(rule_string)
        else:
            return rrulestr(f"RRULE:{rule_string}")
    except Exception:
        return None


def get_next_occurrence(recurrence_rule: str, after: datetime | None = None) -> datetime | None:
    if not recurrence_rule:
        return None

    rule = parse_recurrence_rule(recurrence_rule)
    if not rule:
        return None

    start = after or utcnow()
    try:
        return rule.after(start)
    except Exception:
        return None


def expand_recurring_task(
    recurrence_rule: str,
    start_date: datetime,
    end_date: datetime | None = None,
    limit: int = 100,
) -> list[datetime]:
    if not recurrence_rule:
        return []

    rule = parse_recurrence_rule(recurrence_rule)
    if not rule:
        return []

    try:
        occurrences = []
        count = 0

        for occurrence in rule:
            if count >= limit:
                break
            if occurrence >= start_date:
                if end_date and occurrence > end_date:
                    break
                occurrences.append(occurrence)
                count += 1

        return occurrences
    except Exception:
        return []


def parse_simple_recurrence(recurrence: str, base_date: datetime) -> list[datetime]:
    recurrence = recurrence.lower().strip()
    occurrences = []

    if recurrence == "daily":
        for i in range(30):
            occurrences.append(base_date + timedelta(days=i))
    elif recurrence == "weekly":
        for i in range(12):
            occurrences.append(base_date + timedelta(weeks=i))
    elif recurrence == "monthly":
        current = base_date
        for i in range(12):
            occurrences.append(current)
            if current.month == 12:
                current = current.replace(year=current.year + 1, month=1)
            else:
                current = current.replace(month=current.month + 1)

    return occurrences
