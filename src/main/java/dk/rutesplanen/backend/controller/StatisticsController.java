package dk.rutesplanen.backend.controller;

import dk.rutesplanen.backend.model.Expense;
import dk.rutesplanen.backend.model.Pickup;
import dk.rutesplanen.backend.model.PickupRequest;
import dk.rutesplanen.backend.model.PickupStatus;
import dk.rutesplanen.backend.repositories.ExpenseRepository;
import dk.rutesplanen.backend.repositories.PickupRepository;
import dk.rutesplanen.backend.repositories.PickupRequestRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-controller til statistik-dashboard.
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final PickupRequestRepository pickupRequestRepository;
    private final PickupRepository pickupRepository;
    private final ExpenseRepository expenseRepository;

    public StatisticsController(PickupRequestRepository pickupRequestRepository,
                                PickupRepository pickupRepository,
                                ExpenseRepository expenseRepository) {
        this.pickupRequestRepository = pickupRequestRepository;
        this.pickupRepository = pickupRepository;
        this.expenseRepository = expenseRepository;
    }

    @GetMapping
    public Map<String, Object> getStatistics() {
        List<PickupRequest> requests = pickupRequestRepository.findAll();
        List<Pickup> pickups = pickupRepository.findAll();
        List<Expense> expenses = expenseRepository.findAll();

        // Kun afsluttede anmodninger tæller med i statistikken
        List<PickupRequest> completed = requests.stream()
                .filter(r -> r.getStatus() == PickupStatus.COMPLETED)
                .collect(Collectors.toList());

        // Samlet antal afsluttede afhentninger og pantposer
        long totalPickups = completed.size();
        long totalBags = completed.stream()
                .filter(r -> r.getPantAmount() != null)
                .mapToLong(PickupRequest::getPantAmount)
                .sum();

        // Samlede omkostninger fra både registrerede afhentninger og separate udgifter
        double totalCostPickups = pickups.stream()
                .filter(p -> p.getCost() != null)
                .mapToDouble(Pickup::getCost).sum();
        double totalCostExpenses = expenses.stream()
                .filter(e -> e.getAmount() != null)
                .mapToDouble(Expense::getAmount).sum();

        // Gruppér afhentninger per måned (format: åååå-mm) til søjlediagram
        Map<String, Long> pickupsByMonth = completed.stream()
                .filter(r -> r.getDate() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getDate().getYear() + "-" + String.format("%02d", r.getDate().getMonthValue()),
                        Collectors.counting()));

        // Summér pantposer per restaurant til søjlediagram
        Map<String, Long> bagsByRestaurant = completed.stream()
                .filter(r -> r.getRestaurant() != null && r.getPantAmount() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getRestaurant().getName(),
                        Collectors.summingLong(PickupRequest::getPantAmount)));

        // Summér udgifter per måned til linjediagram
        Map<String, Double> costsByMonth = expenses.stream()
                .filter(e -> e.getCreatedAt() != null && e.getAmount() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getCreatedAt().getYear() + "-" + String.format("%02d", e.getCreatedAt().getMonthValue()),
                        Collectors.summingDouble(Expense::getAmount)));

        // Tæl antal afhentninger per chauffør til aktivitetsdiagram
        Map<String, Long> chauffeurActivity = pickups.stream()
                .filter(p -> p.getChauffeur() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getChauffeur().getName(),
                        Collectors.counting()));

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("afhentningerTotal", totalPickups);
        stats.put("poserTotal", totalBags);
        stats.put("omkostningerTotal", Math.round((totalCostPickups + totalCostExpenses) * 100.0) / 100.0);
        stats.put("afhentningerPerMaaned", toSortedList(pickupsByMonth));
        stats.put("poserPerRestaurant", toSortedByValueDesc(bagsByRestaurant));
        stats.put("omkostningerPerMaaned", toSortedList(costsByMonth));
        stats.put("chauffeurAktivitet", toSortedByValueDesc(chauffeurActivity));
        return stats;
    }

    private List<Map<String, Object>> toSortedList(Map<String, ?> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("label", e.getKey());
                    m.put("value", e.getValue());
                    return m;
                }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> toSortedByValueDesc(Map<String, Long> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("label", e.getKey());
                    m.put("value", e.getValue());
                    return m;
                }).collect(Collectors.toList());
    }
}
