mean = zeros(400,400);

mean=100/16000.*2000000;

shadmask = zeros(400,400);

shadmask(135:135+64,132:132+32)=1;
shadmask(135:135+64,132+35:132+67)=1;
shadmask(135:135+64,132+70:132+102)=1;
shadmask(135:135+64,132+105:132+137)=1;

shadmask(202:202+64,132:132+32)=1;
shadmask(202:202+64,132+35:132+67)=1;
shadmask(202:202+64,132+70:132+102)=1;
shadmask(202:202+64,132+105:132+137)=1;

shadmask = shadmask';
mean = mean .* shadmask;

msource = 200/16000*2000000*pmask.*shadmask;
mean = mean;% + msource;
obs = poissrnd(mean + msource);

