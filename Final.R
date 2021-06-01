
library(Rmisc)
library("tidyverse")
library("ggplot2")

x_gen_stats <- read.csv("rd100_stats_16.csv", stringsAsFactors=FALSE)

	
x_overall_stats <- x_gen_stats %>% 
	summarize(bestFit = min(bestF), avgF = mean(bestF), stdF = sd(bestF), GCI = (CI(bestF, ci = 0.95)[1] - CI(bestF, ci = 0.95)[2]))

x_avg_best <- x_gen_stats %>% 
	filter(threadID == 0) %>% 
	group_by(G) %>% 
	summarize(F = mean(bestF), stdF = sd(bestF), GCI = (CI(bestF, ci = 0.95)[1] - CI(bestF, ci = 0.95)[2]))

x_avg_best <- mutate(x_avg_best, Type = "Best")

x_avg_avg <- x_gen_stats %>% 
	filter(threadID == 0) %>% 
	group_by(G) %>% 
	summarize(F = mean(avgF), stdF = sd(avgF), GCI = (CI(avgF, ci = 0.95)[1] - CI(avgF, ci = 0.95)[2]))

x_avg_avg <- mutate(x_avg_avg, Type = "Average")

x_avg <- rbind(x_avg_best, x_avg_avg)

x_avg$Type <- as.factor(x_avg$Type)


x_avg <- replace(x_avg, is.na(x_avg), 0)

ggplot(x_avg, aes(x=G, y=F)) +
    geom_errorbar(aes(ymin=F-GCI, ymax=F+GCI, color = Type), size = 1, alpha = 0.5) +
	geom_line(aes(group=Type), size = 0.3, color="black") +
	labs(title="rd100 TSP, island 1 of 16 islands\nAverage and best fitness with 95% confidence intervals averaged over 10 runs", x ="Generation", y = "Fitness") + 
	scale_color_manual(values = c("blue", "red"))


ggsave("rd100 TSP, island 1 of 16 islands.png", width = 7, height = 4)

# -------------------------------------------------------------------------------------------------------------


x <- read.csv("rd100_best_route_16.csv")

ggplot(x, aes(x=x, y=y)) +
	geom_polygon(colour="black", fill=NA) +
	geom_point(color="blue") +
	labs(title="Best Route For rd100 Solved by 16 Islands PGA")

ggsave("Best Route For 100-city random TSP (Reinelt) Solved by 16 Islands PGA.png", width = 7, height = 4)


# -------------------------------------------------------------------------------------------------------------


x_gen_stats <- read.csv("berlin52_times.csv", stringsAsFactors=FALSE)

x_best <- x_gen_stats %>% 
	filter(bestF == x_gen_stats[2,4])

x_avg <- x_gen_stats %>% 
	group_by(threads) %>% 
	summarize(T = mean(time), stdT = sd(time), GCI = (CI(time, ci = 0.95)[1] - CI(time, ci = 0.95)[2]))
	
speedup <- x_avg$T[1] / x_avg$T
view(speedup)

ggplot(x_avg, aes(x=threads, y=T)) +
    geom_errorbar(aes(ymin=T-GCI, ymax=T+GCI), width = 0.15, size = 0.8, alpha = 0.7, color="red") +
	geom_line(size = 0.5, color="black") +
	geom_point(alpha = 0.7) +
	labs(title="Execution Time vs Number of Threads for Berlin52 TSP\nAverage execution time with 95% confidence intervals averaged over 10 runs", x ="Number of Threads", y = "Execution Time (s)")  + 
	scale_x_continuous(breaks = c(1,2,4,8,16,32), trans = 'log2') 


ggsave("Execution Time vs Number of Threads for Berlin52 TSP.png", width = 7, height = 4)

# -------------------------------------------------------------------------------------------------------------

x_gen_stats <- read.csv("pr76_times.csv", stringsAsFactors=FALSE)

x_best <- x_gen_stats %>% 
	filter(bestF == x_gen_stats[7,4])

x_avg <- x_gen_stats %>% 
	group_by(threads) %>% 
	summarize(T = mean(time), stdT = sd(time), GCI = (CI(time, ci = 0.95)[1] - CI(time, ci = 0.95)[2]))

speedup <- x_avg$T[1] / x_avg$T
view(speedup)

ggplot(x_avg, aes(x=threads, y=T)) +
    geom_errorbar(aes(ymin=T-GCI, ymax=T+GCI), width = 0.15, size = 0.8, alpha = 0.7, color="red") +
	geom_line(size = 0.5, color="black") +
	geom_point(alpha = 0.7) +
	labs(title="Execution Time vs Number of Threads for pr76 TSP\nAverage execution time with 95% confidence intervals averaged over 10 runs", x ="Number of Threads", y = "Execution Time (s)")  + 
	scale_x_continuous(breaks = c(1,2,4,8,16,32), trans = 'log2') +
	scale_y_continuous(breaks = seq(25, 120, 15))


ggsave("Execution Time vs Number of Threads for pr76 TSP.png", width = 7, height = 4)

# -------------------------------------------------------------------------------------------------------------

x_gen_stats <- read.csv("rd100_times.csv", stringsAsFactors=FALSE)

x_best <- x_gen_stats %>% 
	filter(bestF == x_gen_stats[1,4])

x_avg <- x_gen_stats %>% 
	group_by(threads) %>% 
	summarize(T = mean(time), stdT = sd(time), GCI = (CI(time, ci = 0.95)[1] - CI(time, ci = 0.95)[2]))

speedup <- x_avg$T[1] / x_avg$T
view(speedup)

ggplot(x_avg, aes(x=threads, y=T)) +
    geom_errorbar(aes(ymin=T-GCI, ymax=T+GCI), width = 0.15, size = 0.8, alpha = 0.7, color="red") +
	geom_line(size = 0.5, color="black") +
	geom_point(alpha = 0.7) +
	labs(title="Execution Time vs Number of Threads for rd100 TSP\nAverage execution time with 95% confidence intervals averaged over 10 runs", x ="Number of Threads", y = "Execution Time (s)")  + 
	scale_x_continuous(breaks = c(1,2,4,8,16,32), trans = 'log2') 


ggsave("Execution Time vs Number of Threads for rd100 TSP.png", width = 7, height = 4)

# -------------------------------------------------------------------------------------------------------------





