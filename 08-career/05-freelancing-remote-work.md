# Career — Lesson 5: Freelancing & Remote Work

## Why Freelancing?

```
Full-Time:                           Freelancing:
┌──────────────────┐                 ┌──────────────────┐
│ One employer     │                 │ Multiple clients  │
│ Fixed salary     │                 │ Set your rates    │
│ 9-5 schedule     │                 │ Flexible hours    │
│ Office/PTO       │                 │ Work anywhere     │
│ Career ladder    │                 │ You're the CEO   │
│                  │                 │                  │
│ Stability ✅     │                 │ Freedom ✅        │
│ Limited income ❌│                 │ Higher income ✅  │
│ Less control ❌  │                 │ More risk ❌      │
└──────────────────┘                 └──────────────────┘
```

## Getting Started

### Skills in Demand

| Skill | Rate Range (USD) | Demand |
|-------|------------------|--------|
| Spring Boot / Microservices | $80-150/hr | Very High |
| Spring Security / Auth | $100-180/hr | High |
| Java + AWS | $100-160/hr | Very High |
| Java + Kafka | $90-150/hr | High |
| Java + Android | $60-120/hr | Medium |
| Java + Big Data (Spark) | $100-180/hr | High |
| Legacy Java (J2EE) | $80-140/hr | Medium |
| Code Review / Architecture | $120-200/hr | High |

### Platforms

```
Upwork:          General freelancing, competitive
Toptal:          Elite network (pass their test), higher rates
LinkedIn:        Best for direct client relationships
Freelancer:      Project-based, wide range
Fiverr:          Smaller projects, fixed-price
Guru:            Good for long-term contracts
```

## Setting Your Rates

```markdown
Rate Calculation:

Target Annual Income: $150,000
Billable Days/Year: 220 (52 weeks − 4 weeks vacation − 2 weeks sick − holidays)
Billable Hours/Day: 6 (not 8 — you need admin, marketing, learning)

Hourly Rate = $150,000 / (220 × 6) = ~$114/hr

Round up to $125/hr for negotiations.
Raise rates 10-20% every 6-12 months.

Fixed Price: Estimate hours × hourly rate × 1.5 (risk buffer)
```

## Finding Clients

```markdown
**Warm Outreach (Best):**
- Former colleagues and managers
- LinkedIn connections
- Twitter/X presence
- Conference talks

**Cold Outreach:**
- Email CTOs of companies using Java
- Apply on job boards for contract roles
- Reply to "Looking for Java dev" posts
- Contribute to open source → get noticed

**Inbound (Build over time):**
- Blog about Java topics
- YouTube tutorials
- GitHub projects
- Stack Overflow answers
```

## Contracts & Legal

```markdown
**Every project needs a contract:**

1. Scope of Work (what you'll build)
2. Timeline (milestones + deadlines)
3. Payment Terms (rate, schedule, late fees)
4. Intellectual Property (who owns the code)
5. Confidentiality (NDA clause)
6. Termination (notice period, kill fee)
7. Liability (limit your exposure)

**Payment Schedule:**
- 25-50% upfront (especially for new clients)
- 25-50% at midpoint milestone
- 25% on completion/delivery
- Never work 100% on credit
```

## Client Management

```java
// The "Scope Creep" prevention pattern
public class ContractManager {

    private final List<ChangeRequest> changes = new ArrayList<>();

    public void handleNewRequest(String request) {
        if (isInScope(request)) {
            // Included in original price
            addToBacklog(request);
        } else {
            // Out of scope — new change request
            ChangeRequest cr = new ChangeRequest(request, estimateHours(request));
            changes.add(cr);
            sendForApproval(cr);
        }
    }

    private void sendForApproval(ChangeRequest cr) {
        emailClient.send("""
            Subject: Change Request: %s

            Hi %s,

            The following request is outside the original scope:
            %s

            Estimated additional cost: $%d
            Estimated additional time: %d days

            Please approve to proceed.

            Thanks,
            %s
            """, cr.description(), clientName, cr.description(),
            cr.estimatedCost(), cr.estimatedDays(), yourName);
    }
}
```

## Remote Work Setup

```markdown
**Essential Tools:**
- Laptop: MBP or ThinkPad with 32GB+ RAM, fast CPU
- Monitor: 27"+ 4K or dual monitors
- Audio: Quality headset (Jabra/Logitech) + external mic
- Video: Good webcam (Logitech Brio or similar)
- Network: Wired ethernet or mesh Wi-Fi → VPN → backup LTE
- UPS: Battery backup for power outages

**Software Stack:**
- Communication: Slack, Discord, Telegram
- Video: Zoom, Google Meet
- Project Management: Jira, Linear, Notion
- Time Tracking: Toggl, Harvest
- Invoicing: FreshBooks, Wave
- VPN: Tailscale, WireGuard
- Password Manager: 1Password, Bitwarden
```

## Tax & Finance

```markdown
**For US Freelancers:**
- Pay estimated quarterly taxes (1040-ES)
- Deduct business expenses (equipment, software, home office, internet)
- Consider S-Corp election ($60K+ income)
- SEP IRA or Solo 401(k) for retirement
- Health insurance via ACA marketplace

**For Non-US Freelancers:**
- Register as sole trader or LLC equivalent
- Understand VAT/GST obligations
- Withholding tax on US clients (W-8BEN form)
- Currency exchange (Wise/Revolut for multi-currency)
```

## Exercises

1. Calculate your freelance hourly rate based on your target income.
2. Draft a scope of work for a hypothetical Java project.
3. Write a "scope creep" email template politely asking for additional budget.
4. Set up a time tracking tool and use it for a week.
5. Create a profile on Upwork or Toptal (or update your LinkedIn for contracting).
